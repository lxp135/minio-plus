package org.liuxp.minioplus.s3.def;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ListParts {

    private String bucketName;

    private String objectName;

    private int maxParts;

    private String uploadId;

    private List<Part> partList = null;

    public static ListParts build(){
        ListParts listParts = new ListParts();
        listParts.setPartList(new ArrayList<>());
        return listParts;
    }

    @Getter
    @Setter
    @ToString
    public static class Part{
        private int partNumber;

        private String etag;

        private ZonedDateTime lastModified;

        private Long size;
    }

    public void addPart(int partNumber, String etag, ZonedDateTime lastModified, Long size){

        Part part = new Part();
        part.setPartNumber(partNumber);
        part.setEtag(etag);
        part.setLastModified(lastModified);
        part.setSize(size);

        if(this.partList == null){
            partList = new ArrayList<>();
        }
        partList.add(part);
    }

}
