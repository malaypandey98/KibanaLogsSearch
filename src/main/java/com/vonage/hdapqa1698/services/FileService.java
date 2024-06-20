package com.vonage.hdapqa1698.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

@Component
@Slf4j
@AllArgsConstructor
public class FileService {

    public void writeResult(File file, boolean deleteFileIfExist, String... data) throws IOException {
        if(deleteFileIfExist){
            deleteFileIfExists(file);
        }
        createFileIfNotExists(file);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file.getName(), true))) {
            for (String dataItem : data) {
                writer.append(dataItem).append("\n");
            }
            writer.append("\n");
        }
    }

    public String readFrom(File file) throws IOException {
        if(Objects.isNull(file) || !file.exists()){
            return "";
        }
        StringBuilder contentBuilder = new StringBuilder();
        try(Scanner reader = new Scanner(file)){
            while (reader.hasNextLine()) {
                contentBuilder.append(reader.nextLine()).append("\n");
            }
            return contentBuilder.toString();
        } catch (FileNotFoundException e) {
            log.error("Unable to read file {}. ERROR: {}", file.getAbsoluteFile(), e.getMessage());
        }
        return "";
    }

    public void createFileIfNotExists(File file) throws IOException {
        if(!file.exists()){
            log.info("Creating new file {}, Result {}", file.getAbsolutePath(), file.createNewFile());
        }
    }

    public void deleteFileIfExists(File file) throws IOException {
        if (file.exists()) {
            log.info("Deleting file {} status {}", file.getAbsoluteFile(), file.delete());
        }
    }
}