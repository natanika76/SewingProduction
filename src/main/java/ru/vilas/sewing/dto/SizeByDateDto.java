package ru.vilas.sewing.dto;

import lombok.Data;

import java.util.List;
@Data
public class SizeByDateDto {
   private Long sizeByDateId;
   private String size;
   private String height;
   private Integer quantity;
   private Integer done;
   private Integer fullDone;
   private Integer cutOut;
   private Integer fullCutOut;
}
