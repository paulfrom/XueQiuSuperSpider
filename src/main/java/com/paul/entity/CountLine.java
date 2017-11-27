package com.paul.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by liusonglin
 * Date:2017/10/26
 * Description:
 */
@ToString
public class CountLine {
    @Setter
    @Getter
    private Long count;

    @Setter
    private Date date;

    private String weekend;


    public int getWeekend(){
        LocalDate localDate = LocalDateTime
                .ofInstant(date.toInstant(), ZoneId.systemDefault())
                .toLocalDate();
        return localDate.getDayOfWeek().getValue();
    }


    public String getDate(){
        LocalDate localDate = LocalDateTime
                .ofInstant(date.toInstant(), ZoneId.systemDefault())
                .toLocalDate();
        return localDate.toString();
    }
}
