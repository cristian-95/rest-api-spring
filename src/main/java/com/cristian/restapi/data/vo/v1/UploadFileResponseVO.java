package com.cristian.restapi.data.vo.v1;

import java.io.Serializable;

public class UploadFileResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileName;
    private String fileDownloadURL;
    private String fileType;
    private long fileSize;

    public UploadFileResponseVO() {
    }

    public UploadFileResponseVO(String fileName, String fileDownloadURL, String fileType, long fileSize) {
        this.fileName = fileName;
        this.fileDownloadURL = fileDownloadURL;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDownloadURL() {
        return fileDownloadURL;
    }

    public void setFileDownloadURL(String fileDownloadURL) {
        this.fileDownloadURL = fileDownloadURL;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


}
