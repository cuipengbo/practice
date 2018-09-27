package com.hytx.bobo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hytx.bobo.dao.OptionDao;
import com.hytx.bobo.model.TOptions;

@Service
public class OptionService {

	@Autowired
	private OptionDao optionDao;
	
	public List<TOptions> findAll() {
		return optionDao.selectAll();
	}

}
