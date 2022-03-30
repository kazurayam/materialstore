package com.kazurayam.materialstore.filesystem;

import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.map.IdentityMapper;
import com.kazurayam.materialstore.map.MappedResultSerializer;
import com.kazurayam.materialstore.map.Mapper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class StoreImpl implements Store {

    private static final Logger logger = LoggerFactory.getLogger(StoreImpl.class);
    public static final Store NULL_OBJECT = new StoreImpl( null );

    private final Path root_;
    private final Set<Jobber> jobberCache_;
    private static final int BUFFER_SIZE = 8000;

    public StoreImpl(Path root) {
        try {
            if (root == null) {
                root = Files.createTempDirectory("TempDirectory");
            }
            // ensure the root directory to exist
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            this.root_ = root;
            this.jobberCache_ = new HashSet<>();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public int copyMaterials(JobName jobName, JobTimestamp source, JobTimestamp target) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        MaterialList sourceMaterialList = this.select(jobName, source, QueryOnMetadata.ANY);
        MappedResultSerializer serializer = new MappedResultSerializer(this, jobName, target);
        Mapper identity = new IdentityMapper();
        identity.setStore(this);
        identity.setMappingListener(serializer);
        int count = 0;
        Iterator<Material> iter = sourceMaterialList.iterator();
        while (iter.hasNext()) {
            Material material = iter.next();
            try {
                identity.map(material);
            } catch (IOException e) {
                throw new MaterialstoreException(e);
            }
            count += 1;
        }

        return count;
    }

    @Override
    public int deleteMaterialsOlderThanExclusive(final JobName jobName,
                                                 JobTimestamp jobTimestamp,
                                                 final long amountToSubtract,
                                                 TemporalUnit unit)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(jobTimestamp);
        if (amountToSubtract < 0) {
            throw new IllegalArgumentException("amountToSubtract(" + amountToSubtract + ") must not be a negative value < 0");
        }

        Objects.requireNonNull(unit);
        // calculate the base timestamp
        JobTimestamp thanThisJobTimestamp = jobTimestamp.minus(amountToSubtract, unit);
        // identify the JobTimestamp directories to be deleted
        List<JobTimestamp> toBeDeleted = this.findAllJobTimestampsPriorTo(jobName, thanThisJobTimestamp);
        // now delete files/directories
        int countDeletedJT = 0;
        for (JobTimestamp jt : toBeDeleted) {
            Path dir = root_.resolve(jobName.toString()).resolve(jt.toString());
            // delete this directory recursively
            if (Files.exists(dir)) {
                try {
                    Files.walk(dir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    throw new MaterialstoreException(e);
                }
                countDeletedJT += 1;
            }
        }
        return countDeletedJT;
    }

    /**
     * @param jobName JobName instance
     * @return List of JobTimestamp objects in the jobName directory.
     * The returned list is sorted in reverse order (the latest timestamp should come first)
     */
    @Override
    public List<JobTimestamp> findAllJobTimestamps(final JobName jobName)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Path jobNameDir = root_.resolve(jobName.toString());
        if (Files.exists(jobNameDir)) {
            try {
                List<JobTimestamp> jobTimestamps =
                        Files.list(jobNameDir)
                                .filter(Files::isDirectory)
                                .map(p -> p.getFileName().toString())
                                .filter(JobTimestamp::isValid)
                                .map(JobTimestamp::new)
                                // sort the list in reverse order (the latest timestamp should come first)
                                .sorted(Collections.reverseOrder())
                                .collect(Collectors.toList());

                logger.debug(String.format("[findAllJobTimestamps] jobName=%s", jobName));
                logger.debug(String.format("[findAllJobTimestamps] jobTimestamps.size()=%d", jobTimestamps.size()));
                Iterator<JobTimestamp> iter = jobTimestamps.iterator();
                int index = 0;
                while (iter.hasNext()) {
                    JobTimestamp jt = iter.next();
                    index += 1;
                    logger.debug(String.format("[findAllJobTimestamps] jt[%d]=%s", index, jt.toString()));
                }
                return jobTimestamps;
            } catch (IOException e) {
                throw new MaterialstoreException(e);
            }
        } else {
            throw new MaterialstoreException("JobName \"" + jobName + "\" is not found in " + root_);
        }
    }

    @Override
    public List<JobTimestamp> findAllJobTimestampsPriorTo(JobName jobName,
                                                          final JobTimestamp baseJobTimestamp)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(baseJobTimestamp);
        List<JobTimestamp> all = findAllJobTimestamps(jobName);
        return all.stream()
                        .filter( jt -> {
                            int comp = jt.compareTo(baseJobTimestamp);
                            return comp < 0;
                        })
                        .collect(Collectors.toList());
    }

    @Override
    public JobTimestamp findJobTimestampPriorTo(JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(jobTimestamp);
        List<JobTimestamp> all = findAllJobTimestampsPriorTo(jobName, jobTimestamp);
        if (all.size() > 0) {
            return all.get(0);
        } else {
            return JobTimestamp.NULL_OBJECT;
        }
    }

    @Override
    public JobTimestamp findLatestJobTimestamp(JobName jobName) throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        List<JobTimestamp> all = findAllJobTimestamps(jobName);
        if (all.size() > 0) {
            return all.get(0);
        } else {
            return JobTimestamp.NULL_OBJECT;
        }

    }

    public Jobber getCachedJobber(JobName jobName, JobTimestamp jobTimestamp) {
        Jobber result = null;
        for (int i = 0; i < jobberCache_.size() ; i++) {
            Jobber cached = DefaultGroovyMethods.getAt(jobberCache_, i);
            assert cached != null;
            if (cached.getJobName().equals(jobName) && cached.getJobTimestamp().equals(jobTimestamp)) {
                result = cached;
                break;
            }
        }
        return result;
    }

    /**
     * return an instance of Job.
     * if cached, return the found.
     * if not cached, return the new one.
     *
     */
    @Override
    public Jobber getJobber(JobName jobName, JobTimestamp jobTimestamp) throws MaterialstoreException {
        Jobber jobber = getCachedJobber(jobName, jobTimestamp);
        if (jobber != null) {
            return jobber;
        } else {
            Jobber newJob = new Jobber(this, jobName, jobTimestamp);
            // put the new Job object in the cache
            jobberCache_.add(newJob);
            return newJob;
        }

    }

    @Override
    public Path getPathOf(Material material) {
        Objects.requireNonNull(material);
        Path relativePath = material.getRelativePath();
        return this.getRoot().resolve(relativePath);
    }

    @Override
    public Path getRoot() {
        return root_;
    }

    @Override
    public List<JobTimestamp> queryAllJobTimestamps(final JobName jobName,
                                                    final QueryOnMetadata query)
            throws MaterialstoreException {
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(query);
        List<JobTimestamp> all = findAllJobTimestamps(jobName);

        logger.debug(String.format("[queryAllJobTimestamps] all.size()=%d", all.size()));
        IntStream.range(0, all.size())
                .forEach(index -> {
                    JobTimestamp ajt = all.get(index);
                    logger.debug(String.format("[queryAllJobTimestamps] ajt[%d]=%s", index, ajt.toString()));
                });

        final List<JobTimestamp> filtered = new ArrayList<>();
        IntStream.range(0, all.size())
                .forEach(index -> {
                    JobTimestamp jobTimestamp = all.get(index);
                    // select Material objects that match with the query given
                    try {
                        final MaterialList materialList;
                        materialList = StoreImpl.this.select(jobName, jobTimestamp, query);
                        String msg = "[queryAllJobTimestamps] materialList.size()=" +
                                materialList.size() + ", query=" + query;
                        if (materialList.size() > 0) {
                            logger.debug(msg);
                            filtered.add(jobTimestamp);
                        } else {
                            logger.info(msg);
                        }
                    } catch (MaterialstoreException e) {
                        e.printStackTrace();
                    }
                });

        logger.debug(String.format("[queryAllJobTimestamps] filtered.size()=%d", filtered.size()));
        IntStream.range(0, filtered.size())
                .forEach(index -> {
                    JobTimestamp fjt = filtered.get(index);
                    logger.debug(String.format("[queryAllJobTimestamps] fjt[%d]=%s",
                            index, fjt.toString()));
                });

        return filtered;
    }

    @Override
    public List<JobTimestamp> queryAllJobTimestampsPriorTo(JobName jobName,
                                                           QueryOnMetadata query,
                                                           final JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Objects.requireNonNull(jobTimestamp);
        List<JobTimestamp> all = this.queryAllJobTimestamps(jobName, query);

        logger.debug(String.format("[queryAllJobTimestampsPriorTo] all.size()=%d", all.size()));
        IntStream.range(0, all.size())
                .forEach(index -> {
                    JobTimestamp ajt = all.get(index);
                    logger.debug(String.format("[queryAllJobTimestampsPriorTo] aft[%d]=%s",
                            index, ajt.toString()));
                });

        List<JobTimestamp> filtered =
                all.stream()
                        .filter(jt -> {
                            int comp = jt.compareTo(jobTimestamp);
                            return comp < 0;
                        })
                        .collect(Collectors.toList());

        logger.debug(String.format("[queryAllJobTimestampsPriorTo] filtered.size()=%d", filtered.size()));
        IntStream.range(0, filtered.size())
                        .forEach(index -> {
                            JobTimestamp fjt = filtered.get(index);
                                    logger.debug(String.format("[queryAllJobTimestampsPriorTo] jft[%d]=%s",
                                            index, fjt.toString()));
                        });

        return filtered;
    }

    @Override
    public JobTimestamp queryJobTimestampPriorTo(JobName jobName,
                                                 QueryOnMetadata query,
                                                 JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        List<JobTimestamp> all = queryAllJobTimestampsPriorTo(jobName, query, jobTimestamp);
        logger.debug(String.format("[queryJobTimestampPriorTo] all.size()=%d", all.size()));
        if (all.size() > 0) {
            return all.get(0);
        } else {
            return JobTimestamp.NULL_OBJECT;
        }

    }

    @Override
    public MaterialList reflect(MaterialList base) throws MaterialstoreException {
        Objects.requireNonNull(base);
        logger.debug(String.format("[queryMaterialListWithSimilarContentPriorTo] base.size()=%d", base.size()));
        assert base.size() > 0;
        List<JobTimestamp> allJobTimestamps = queryAllJobTimestampsPriorTo(base.getJobName(), base.getQueryOnMetadata(), base.getJobTimestamp());
        logger.debug(String.format("[queryMaterialListWithSimilarContentPriorTo] allJobTimestamps.size()=%d", allJobTimestamps.size()));
        for (JobTimestamp previous : allJobTimestamps) {
            logger.debug(String.format("[queryMaterialListWithSimilarContentPriorTo] previous=%s", previous));

            MaterialList candidate = select(base.getJobName(), previous, base.getQueryOnMetadata());
            if (similar(base, candidate)) {
                logger.debug(String.format("[queryMaterialListWithSimilarContentPriorTo] previous=%s is similar to base=%s", previous.toString(), base.getJobTimestamp()));
                MaterialList collected = collect(base, candidate);
                logger.debug(String.format("[queryMaterialListWithSimilarContentPriorTo] collected.size()=%d", collected.size()));
                return collected;
            } else {
                logger.debug(String.format("[queryMaterialListWithSimilarContentPriorTo] previous=%s is not similar to base=%s", previous.toString(), base.getJobTimestamp()));
            }
        }
        return MaterialList.NULL_OBJECT;
    }

    private static boolean similar(MaterialList baseList, MaterialList targetList) {
        int count = 0;
        Iterator<Material> iter = baseList.iterator();
        while (iter.hasNext()) {
            Material base = iter.next();
            if (targetList.containsMaterialsSimilarTo(base)) {
                count += 1;
            }
        }
        return count == baseList.size();
    }

    /**
     * lookup the Materials amongst the targetList which are similar to the baseList
     * while ignoring non-similar Materials in the targetList,
     * then return the collection of the found Materials
     *
     */
    private static MaterialList collect(MaterialList baseList,
                                        MaterialList targetList) {
        MaterialList collection =
                new MaterialList(targetList.getJobName(),
                        targetList.getJobTimestamp(),
                        targetList.getQueryOnMetadata());
        Iterator<Material> iter = baseList.iterator();
        while (iter.hasNext()) {
            Material base = iter.next();
            List<Material> found = targetList.findMaterialsSimilarTo(base);
            if (found.size() > 0) {
                collection.add(found);
            }
        }
        //assert collection.countMaterialsWithIdStartingWith("5d7e467") <= 1
        return collection;
    }

    @Override
    public JobTimestamp queryLatestJobTimestamp(JobName jobName,
                                                QueryOnMetadata query)
            throws MaterialstoreException {
        List<JobTimestamp> all = queryAllJobTimestamps(jobName, query);
        if (all.size() > 0) {
            return all.get(0);
        } else {
            return JobTimestamp.NULL_OBJECT;
        }

    }

    @Override
    public byte[] read(Material material) throws MaterialstoreException {
        Objects.requireNonNull(material);
        Jobber jobber =
                this.getJobber(material.getJobName(),
                        material.getJobTimestamp());
        return jobber.read(material);
    }

    @Override
    public List<String> readAllLines(Material material) throws MaterialstoreException {
        return readAllLines(material, StandardCharsets.UTF_8);
    }

    @Override
    public List<String> readAllLines(Material material, Charset charset) throws MaterialstoreException {
        Objects.requireNonNull(material);
        if (material.getDiffability() == FileTypeDiffability.AS_TEXT) {
            List<String> lines = new ArrayList<>();
            byte[] bytes = read(material);
            String s = new String(bytes, charset);
            BufferedReader br = new BufferedReader(new StringReader(s));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                throw new MaterialstoreException(e);
            }
            return lines;
        } else {
            throw new MaterialstoreException("unable to read " + material.getRelativeURL() + " as text");
        }
    }


    @Override
    public MaterialList select(JobName jobName,
                               JobTimestamp jobTimestamp,
                               QueryOnMetadata query,
                               FileType fileType)
            throws MaterialstoreException {
        Jobber jobber = this.getJobber(jobName, jobTimestamp);
        return jobber.selectMaterials(query, fileType);
    }

    @Override
    public MaterialList select(JobName jobName,
                               JobTimestamp jobTimestamp,
                               QueryOnMetadata query)
            throws MaterialstoreException {
        Jobber jobber = this.getJobber(jobName, jobTimestamp);
        return jobber.selectMaterials(query);
    }

    @Override
    public MaterialList select(JobName jobName,
                               JobTimestamp jobTimestamp,
                               FileType fileType)
            throws MaterialstoreException {
        Jobber jobber = this.getJobber(jobName, jobTimestamp);
        return jobber.selectMaterials(QueryOnMetadata.ANY, fileType);
    }

    @Override
    public MaterialList select(JobName jobName, JobTimestamp jobTimestamp)
            throws MaterialstoreException {
        Jobber jobber = this.getJobber(jobName, jobTimestamp);
        return jobber.selectMaterials(QueryOnMetadata.ANY);
    }

    @Override
    public Material selectSingle(JobName jobName,
                                 JobTimestamp jobTimestamp,
                                 QueryOnMetadata query,
                                 FileType fileType)
            throws MaterialstoreException {
        Jobber jobber = this.getJobber(jobName, jobTimestamp);
        MaterialList materials = jobber.selectMaterials(query, fileType);
        if (materials.size() > 0) {
            return materials.get(0);
        } else {
            return null;
        }

    }

    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream);
        byte[] buff = new byte[BUFFER_SIZE];
        int bytesRead;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buff)) != -1) {
            baos.write(buff, 0, bytesRead);
        }
        inputStream.close();
        return baos.toByteArray();
    }

    @Override
    public Material write(JobName jobName,
                          JobTimestamp jobTimestamp,
                          FileType fileType,
                          Metadata meta,
                          BufferedImage input)
            throws MaterialstoreException {
        Objects.requireNonNull(input);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(input, fileType.getExtension(), baos);
            byte[] data = baos.toByteArray();
            baos.close();
            return this.write(jobName, jobTimestamp, fileType, meta, data);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public Material write(JobName jobName,
                          JobTimestamp jobTimestamp,
                          FileType fileType,
                          Metadata meta,
                          byte[] input) throws MaterialstoreException {
        Objects.requireNonNull(root_);
        Objects.requireNonNull(jobName);
        Objects.requireNonNull(jobTimestamp);
        Objects.requireNonNull(meta);
        Objects.requireNonNull(fileType);
        Jobber jobber = this.getJobber(jobName, jobTimestamp);
        return jobber.write(input, fileType, meta);
    }

    @Override
    public Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                          File input) throws MaterialstoreException {
        Objects.requireNonNull(input);
        assert input.exists();
        try {
            FileInputStream fis = new FileInputStream(input);
            byte[] data = toByteArray(fis);
            fis.close();
            return this.write(jobName, jobTimestamp, fileType, meta, data);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                          Path input) throws MaterialstoreException {
        Objects.requireNonNull(input);
        assert Files.exists(input);
        try {
            FileInputStream fis = new FileInputStream(input.toFile());
            byte[] data = toByteArray(fis);
            fis.close();
            return this.write(jobName, jobTimestamp, fileType, meta, data);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                          String input, Charset charset) throws MaterialstoreException {
        Objects.requireNonNull(input);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Writer wrt = new BufferedWriter(new OutputStreamWriter(baos, charset.name()));
            wrt.write(input);
            wrt.flush();
            byte[] data = baos.toByteArray();
            wrt.close();
            return this.write(jobName, jobTimestamp, fileType, meta, data);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }

    @Override
    public Material write(JobName jobName, JobTimestamp jobTimestamp, FileType fileType, Metadata meta,
                          String input) throws MaterialstoreException {
        return write(jobName, jobTimestamp, fileType, meta, input, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return root_.normalize().toString();
    }


}
