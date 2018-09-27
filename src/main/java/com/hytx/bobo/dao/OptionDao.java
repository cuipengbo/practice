package com.hytx.bobo.dao;

import org.apache.ibatis.annotations.Mapper;

import tk.mybatis.mapper.common.BaseMapper;

import com.hytx.bobo.model.TOptions;

@Mapper
public interface OptionDao  extends BaseMapper<TOptions>{

}
