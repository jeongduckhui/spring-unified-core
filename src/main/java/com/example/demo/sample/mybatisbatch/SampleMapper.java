package com.example.demo.sample.mybatisbatch;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SampleMapper {

    int insertOne(SampleDto dto);

    int updateOne(SampleDto dto);

    int deleteOne(Long id);
}