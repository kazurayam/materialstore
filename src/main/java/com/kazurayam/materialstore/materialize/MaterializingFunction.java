package com.kazurayam.materialstore.materialize;

import com.kazurayam.materialstore.MaterialstoreException;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

@FunctionalInterface
public interface MaterializingFunction<TargetURL, WebDriver, StorageDirectory> {

    void accept(TargetURL targetURL, WebDriver driver, StorageDirectory storageDirectory) throws MaterialstoreException;

}
