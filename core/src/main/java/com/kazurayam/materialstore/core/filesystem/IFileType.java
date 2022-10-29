package com.kazurayam.materialstore.core.filesystem;

import com.kazurayam.materialstore.core.util.JsonUtil;

import java.util.List;

/**
 * This interface traces the methods of
 * the enum FileType so that users can
 * create his/her own IFileType class for
 * there own cutom FileType.
 */
public interface IFileType extends Jsonifiable, TemplateReady {

    String getExtension();

    FileTypeDiffability getDiffability();

    List<String> getMimeTypes();

    default String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"extension\":\"");
        sb.append(this.getExtension());
        sb.append("\"");
        sb.append(",");
        sb.append("\"mimeTypes\":[");
        int count = 0;
        for (String mimetype: this.getMimeTypes()) {
            if (count > 0) {
                sb.append(",");
            }
            count += 1;
            sb.append("\"");
            sb.append(mimetype);
            sb.append("\"");
        }
        sb.append("]");
        sb.append(",\"diffability\":\"");
        sb.append(this.getDiffability());
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }

    default String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }
    }

}
