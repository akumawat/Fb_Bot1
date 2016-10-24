package com.paypal.dt.bi.controller;


import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;



@Controller
public class HomeController {
	
	private String uploadFolder = "/uploads";	
	@RequestMapping("/")
	public String hello() {
		System.out.println("++++++++++++++++++++++++++no url specified+++++++++++++++++++++++++++++");
		return "redirect:index";
	}
	
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String showindex(HttpServletRequest request,	HttpServletResponse response,
			HttpSession session,
			Model model) {
		System.out.println("in index");
		//this.createQR(session);
		
		return "FileUploadForm";
	}

	@RequestMapping(value = "/index", method = RequestMethod.POST)
	public String processFormUpload(HttpServletRequest request,	HttpServletResponse response, 
			HttpSession session, Model model,
			@RequestParam("file") MultipartFile fileUpload) {
		System.out.println("in index with POST");
		
		String filePath = getUploadFile(session).getAbsolutePath();
		
		System.out.println(fileUpload.getOriginalFilename());
		System.out.println(fileUpload.getSize());
		System.out.println(fileUpload.getContentType());
		
		File uploadedFile = new File(filePath + File.separator + fileUpload.getOriginalFilename());
		try {
			fileUpload.transferTo(uploadedFile);
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return "FileUploadSuccess";
	}
	
	private File getUploadFile(HttpSession session){
		
		String uploadFolderFullPath = session.getServletContext().getRealPath(uploadFolder);
		System.out.println("uploadFolderFullPath" + uploadFolderFullPath);
		File f = new File(uploadFolderFullPath);
		if(!f.exists()){
			System.out.println("making directory ");
			f.mkdirs();
		}
		return f;
	}	
	
			
			    
	
		
	
}
