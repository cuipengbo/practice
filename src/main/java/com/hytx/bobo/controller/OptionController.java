package com.hytx.bobo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hytx.bobo.model.TOptions;
import com.hytx.bobo.service.OptionService;

@RestController
@RequestMapping("/option")
public class OptionController {

	@Autowired
    private OptionService optionService;
	
   @GetMapping("/findAll")
   public List<TOptions> findAll(){
	   List<TOptions> att = optionService.findAll();
       return att;
   }
   
   
   
}
