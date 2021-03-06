package com.ramostear.unaboot.common.util;

import com.ramostear.unaboot.common.UnaBootConst;
import com.ramostear.unaboot.domain.entity.Theme;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class ThemeUtils {

    /**
     *Upload theme archive and unzip theme file
     * @param request
     * @param file
     * @return
     */
    public static Theme upload(HttpServletRequest request, CommonsMultipartFile file){
        if(file.isEmpty()){
            log.warn("Theme file is empty");
            return null;
        }
        String fileName = file.getOriginalFilename();
        int suffixIndex = fileName.lastIndexOf(".");
        String suffixName = fileName.substring(suffixIndex+1).toLowerCase();
        if(!suffixName.equals("zip")&&!suffixName.equals("rar")){
            log.warn("Not a legal theme file");
            return null;
        }

        String themeName = fileName.substring(0,suffixIndex);
        log.info("file upload root dir:"+UnaBootConst.FILE_UPLOAD_ROOT_DIR);
        String themeFile = UnaBootConst.FILE_UPLOAD_ROOT_DIR+"themes"+UnaBootConst.SEPARATOR+fileName;
        String themeFolder = UnaBootConst.FILE_UPLOAD_ROOT_DIR+"themes"+UnaBootConst.SEPARATOR+themeName;
        File tempFile = new File(themeFile);
        if(!tempFile.exists()){
            tempFile.mkdirs();
        }
        try {
            file.transferTo(tempFile);
        } catch (IOException e) {
            log.error("Failed to upload theme file,msg:{}",e.getMessage());
            return null;
        }
        switch(suffixName){
            case "rar":
                try {
                    ArchiveUtils.unRar(tempFile.getAbsolutePath(),themeFolder);
                } catch (Exception e) {
                    log.error("unzip file error,msg:{}",e.getMessage());
                    return null;
                }
                break;
            case "zip":
                try {
                    ArchiveUtils.unzip(themeFile,themeFolder);
                } catch (IOException e) {
                    log.error("unzip theme file error,msg:{}",e.getMessage());
                    return null;
                }
                break;
            default:
                return null;
        }
        return new Theme(themeName);
    }

    public static boolean deleteFile(String path){
        File file = new File(path);
        return deleteFile(file);
    }


    public static boolean deleteFile(File file){
        if(!file.exists()){
            return false;
        }
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files != null && files.length >0){
                for (File f:files){
                    deleteFile(f);
                }
            }
        }
        return file.delete();
    }

    public static void initDefaultTheme() throws IOException {
        String defaultThemePath = ThemeUtils.class.getClassLoader().getResource("theme/default").getPath();
        log.info("Default theme file path :{}",defaultThemePath);
        File srcFile = new File(defaultThemePath);
        String destPath = UnaBootConst.FILE_UPLOAD_ROOT_DIR+"themes"+UnaBootConst.SEPARATOR+"default";
        log.info("Target default theme path:{}",destPath);
        File destFile = new File(destPath);
        FileUtils.copyDirectory(srcFile,destFile);
    }
}
