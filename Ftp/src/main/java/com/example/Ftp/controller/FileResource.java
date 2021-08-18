package com.example.Ftp.controller;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

@RestController
@RequestMapping("/file")
public class FileResource {
    private ServletContext servletContext;
    private Environment env;
    @Autowired
    public  FileResource(Environment env,ServletContext servletContext)
    {
        this.env=env;
        this.servletContext=servletContext;
    }
    //define a location
    public static final String DIRECTORY = System.getProperty("user.home") + "/Downloads/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") List<MultipartFile> multipartFiles) throws IOException {

        List<String> filenames = new ArrayList<>();
        try {

            String user = env.getProperty("usersftp");
            String pass = env.getProperty("passsftp");
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            String host = env.getProperty("urlsftp");
            boolean checkFileExist=false;
            JSch jSch = new JSch();
            Session session = jSch.getSession(user, host);
            session.setPassword(pass);
            session.setConfig(config);
            session.connect();
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            System.out.println("Session connected " + session.isConnected());
            channelSftp.connect();
            for (MultipartFile file : multipartFiles) {
                checkFileExist=false;
                String filename = StringUtils.cleanPath(file.getOriginalFilename());

                Vector fileList=channelSftp.ls(env.getProperty("dir-in-sftp"));
                for(int i=0;i<fileList.size();i++)
                {
                    ChannelSftp.LsEntry entry= (ChannelSftp.LsEntry) fileList.get(i);
                    if(entry.getFilename().equals(filename))
                    {
                        checkFileExist=true;
                        break;
                    }
                }

                if(!checkFileExist) {
//                    Path fileStorage = Paths.get(DIRECTORY,filename).toAbsolutePath().normalize();
//                    Path fileStorage = Paths.get(filename).toAbsolutePath().normalize();
//                    java.nio.file.Files.copy(file.getInputStream(), fileStorage, StandardCopyOption.REPLACE_EXISTING);
                    filenames.add(filename);
                    InputStream inputStream =  new BufferedInputStream(file.getInputStream());
                    channelSftp.put(inputStream, env.getProperty("dir-in-sftp") + filename);
//                    channelSftp.rm("/ssd/sata/"+filename);
//                    channelSftp.get("/ssd/sata/" + filename, filename);
//                    Files.delete(fileStorage);
                }
            }

            channelSftp.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(filenames);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename) throws IOException, JSchException, SftpException {

        String user = env.getProperty("usersftp");
        String pass = env.getProperty("passsftp");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        String host = env.getProperty("urlsftp");
        boolean checkFileExist=false;
        JSch jSch = new JSch();
        Session session = jSch.getSession(user, host);
        session.setPassword(pass);
        session.setConfig(config);
        session.connect();
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        System.out.println("Session connected " + session.isConnected());
        channelSftp.connect();

        Vector fileList=channelSftp.ls(env.getProperty("dir-in-sftp"));
        for(int i=0;i<fileList.size();i++)
        {
            ChannelSftp.LsEntry entry= (ChannelSftp.LsEntry) fileList.get(i);
            if(entry.getFilename().equals(filename))
            {
                checkFileExist=true;
                break;
            }
        }
        if(!checkFileExist)
        {
            channelSftp.disconnect();
            session.disconnect();
            return ResponseEntity.notFound().build();
        }
//        channelSftp.get(env.getProperty("dir-in-sftp")+filename,filename);
//        Path filePath = Paths.get("").toAbsolutePath().normalize().resolve(filename);
//        if (!Files.exists(filePath)) {
//            throw new FileNotFoundException(filename + " was not found on the server");
//        }
        InputStream stream = channelSftp.get(env.getProperty("dir-in-sftp")+filename);
        InputStreamResource inputStreamResource = new InputStreamResource(stream);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", filename);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;File-Name=" + filename);
//        Files.delete(filePath);
//        channelSftp.disconnect();
//        session.disconnect();
//        return ResponseEntity.ok().
//                contentType(
//                        MediaType.parseMediaType(Files.probeContentType(channelSftp.get(filePath))))
//                .headers(httpHeaders).body(resource);


//        return new ResponseEntity(inputStreamResource,httpHeaders, HttpStatus.OK);
//        return ResponseEntity.ok().headers(httpHeaders).contentType(MediaType.MULTIPART_FORM_DATA).body(inputStreamResource);
        return ResponseEntity.ok().headers(httpHeaders).contentType(MediaType.IMAGE_PNG).body(inputStreamResource);
    }

}
