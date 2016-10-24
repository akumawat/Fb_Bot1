package com.paypal.dt.bi.controller;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;



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
		
		indexData();
				
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
	
	private boolean indexData(){
		boolean result = true;
		Date date  = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		
		Settings settings = Settings.settingsBuilder()
		        .put("cluster.name", "my-application-dev-01").build();
		TransportClient client = null;
		try {
			client = TransportClient.builder().settings(settings).build()
					.addTransportAddress((TransportAddress) new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
			int randomNum=0;
			for(int i = 0; i<20;i++){
				cal.add(Calendar.DATE, 1);
				/*cal = Calendar.getInstance();
				randomNum = (int) (Math.random()*10);
				*/	
				Map<String, Object> json2 = new HashMap<String, Object>();
				/*json2.put("vale",randomNum);*/
				json2.put("vale",i);
				json2.put("postDate",dateFormat.format(cal.getTime()));
				
				System.out.println(dateFormat.format(cal.getTime()));
				IndexResponse response = client.prepareIndex("bi", "rt", Integer.toString(i))
				        .setSource(json2)
				        .get();
			
				try {
		            Thread.sleep(5000);
		        } catch (InterruptedException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }
			}

		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			result = false;
			e.printStackTrace();
		}
		
		
		
			    
		return true;
	}
	
	private boolean getData(int id){
		boolean result = true;
		
		Settings settings = Settings.settingsBuilder()
		        .put("cluster.name", "my-application-dev-01").build();
		TransportClient client = null;
		try {
			client = TransportClient.builder().settings(settings).build()
					.addTransportAddress((TransportAddress) new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
			GetResponse resp = client.prepareGet("library", "books", "1").get();
			System.out.println(resp.getSourceAsString());
			client.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			result = false;
			e.printStackTrace();
		}
				
		return result;
	}
	
	public void createQR(HttpSession session){
		String qrCodeData = "Hello World!";
		
		String filePath = getUploadFile(session).getAbsolutePath() + File.separator +"QRCode.png";
		String outFilePath = getUploadFile(session).getAbsolutePath() + File.separator +"QRCodeSnapShot.png";
		
		System.out.println(filePath);
		
		String charset = "UTF-8"; // or "ISO-8859-1"
		Map hintMap = new HashMap();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

/*		try {
			createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);
		} catch (WriterException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println("QR Code image created successfully!");
		
	/*	try {
			//BufferedImage tmpBfrImage = ImageIO.read(new File(filePath));
			BufferedImage tmpBarCodeBfrImage = tmpBfrImage.getSubimage(0, 0, 100, 100);
			File outputfile = new File(outFilePath);
			ImageIO.write(tmpBarCodeBfrImage, "png", outputfile);

			
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));

            try {
            	System.out.println("get result");
            	Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap,hintMap);
            	System.out.println("qrCodeResult.getText(); " + qrCodeResult.getText());
			} catch (NotFoundException e) {
				System.out.println("exception" + e.getMessage());
			
				e.printStackTrace();
			}
			
		

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		try {
			System.out.println("Data read from QR Code: "
					+ readQRCode(filePath, charset, hintMap));
		} catch (NotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void createQRCode(String qrCodeData, String filePath,
			String charset, Map hintMap, int qrCodeheight, int qrCodewidth)
			throws WriterException, IOException {
		BitMatrix matrix = new MultiFormatWriter().encode(
				new String(qrCodeData.getBytes(charset), charset),
				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
		MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath
				.lastIndexOf('.') + 1), new File(filePath));
	}
	
	public static String readQRCode(String filePath, String charset, Map hintMap)
			throws FileNotFoundException, IOException, NotFoundException {
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
				new BufferedImageLuminanceSource(
						ImageIO.read(new FileInputStream(filePath)))));
		Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap,
				hintMap);
		return qrCodeResult.getText();
	}
	
	
}
