package com.notice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import com.common.ApiResult;
import com.google.gson.Gson;
import com.notice.dto.AttachmentRequest;
import com.notice.dto.AttachmentResponse;
import com.notice.dto.NoticeRequest;
import com.notice.dto.NoticeResponse;
import com.notice.service.NoticeService;

import org.json.simple.JSONObject; 
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;
    
    /*
     * 공지사항 신규 등록
     */
	@PostMapping
    public ResponseEntity<ApiResult<NoticeResponse>> createNotice(@RequestPart("json_str") String jsonStr
    															 ,@RequestPart("file") List<MultipartFile> files) {
		
        NoticeRequest noticeRequest = null;
        
		try {
			JSONParser parser = new JSONParser(); 
			Object obj = parser.parse(jsonStr); 
			JSONObject jsonObj = (JSONObject) obj;
				
			Gson gson = new Gson();
			noticeRequest = gson.fromJson(jsonObj.toString(), NoticeRequest.class);
				
		} catch (ParseException e) {
			e.printStackTrace();
		} 
        
        NoticeResponse noticeResponse = noticeService.createNotice(noticeRequest);
        
        //첨부 파일 저장(공지사항id + 첨부파일id를 prefix로 실제 파일명에 붙여 저장)
        for(int i=0; i<files.size(); i++) {
        	
            if(files.get(i).getContentType() != null) {
            	
            	String prefix = String.valueOf(noticeResponse.getId())
            					+ String.valueOf(noticeResponse.getAttachmentResponses().get(i).getId())
            					+ "_"
            					;
            	
            	noticeService.store(prefix, files.get(i));
            }
        }
        
        ApiResult<NoticeResponse> apiResult = ApiResult.success(noticeResponse, HttpStatus.CREATED);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResult);
    }
	
	/*
     * 공지사항 id별 첨부파일별 다운로드(파일명으로 호출)
     */
    @GetMapping(value="/download")
    public ResponseEntity<Resource> serveFile(@RequestParam(value="filename") String filename) {

        Resource file = noticeService.loadAsResource(filename);
        
        String downloadFileName = file.getFilename().substring(file.getFilename().indexOf("_") + 1, file.getFilename().length());
        
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"").body(file);
    }
    
	/*
	 * 공지사항 id별 조회
	 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<NoticeResponse>> getNotice(@PathVariable Long id) {
    	
        NoticeResponse noticeResponse = noticeService.getNotice(id);
        
        ApiResult<NoticeResponse> apiResult = ApiResult.success(noticeResponse, HttpStatus.OK);
        
        return ResponseEntity.status(HttpStatus.OK).body(apiResult);
    }
    
    /*
     * 공지사항 id별 텍스트 데이터 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<NoticeResponse>> updateNotice(@PathVariable Long id,
                                                                  @RequestBody NoticeRequest noticeRequest) {
        
        NoticeResponse noticeResponse = noticeService.updateNotice(id, noticeRequest);
        
        ApiResult<NoticeResponse> apiResult = ApiResult.success(noticeResponse, HttpStatus.OK);
        
        return ResponseEntity.status(HttpStatus.OK).body(apiResult);
    }
    
    /*
     * 첨부파일 id별 수정 (교체)
     * (DB의 파일명은 갱신되나 기존 물리 파일은 지우지 않고 남아 있음)
     */
    @PutMapping("/{noticeId}/attach/{attachmentId}")
    public ResponseEntity<ApiResult<AttachmentResponse>> updateAttachment(@PathVariable Long noticeId,
                                                                          @PathVariable Long attachmentId,
                                                                          @RequestPart("json_str") String jsonStr,
                                                                          @RequestPart("file") List<MultipartFile> files
                                                                          ) {
        
    	AttachmentRequest request = null;
        
		try {
			JSONParser parser = new JSONParser(); 
			Object obj = parser.parse(jsonStr); 
			JSONObject jsonObj = (JSONObject) obj;
				
			Gson gson = new Gson();
			request = gson.fromJson(jsonObj.toString(), AttachmentRequest.class);
				
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
        AttachmentResponse attachmentResponse = noticeService.updateAttachment(noticeId, attachmentId, request);
        
        for(int i=0; i<files.size(); i++) {
        	
            if(files.get(i).getContentType() != null) {
            	
            	String prefix = String.valueOf(noticeId)
            					+ String.valueOf(attachmentId)
            					+ "_"
            					;
            	
            	noticeService.store(prefix, files.get(i));
            }
        }

        ApiResult<AttachmentResponse> apiResult = ApiResult.success(attachmentResponse, HttpStatus.OK);
        
        return ResponseEntity.status(HttpStatus.OK).body(apiResult);
    }
    
    /*
     * 첨부파일 id별 삭제
     */
    @DeleteMapping("/{noticeId}/attach/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long noticeId, @PathVariable Long attachmentId) {
        
        noticeService.deleteAttachment(noticeId, attachmentId);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    /*
     * 공지사항 id별 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        
        noticeService.deleteNotice(id);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
