package com.kazurayam.materialstore.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class PlantUMLUtil {

    public static String standalone(String pumlComponent) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("@startuml");
        pw.println(pumlComponent);
        pw.println("@enduml");
        pw.flush();
        pw.close();
        return sw.toString();
    }
}
