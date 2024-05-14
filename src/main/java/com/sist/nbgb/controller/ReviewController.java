package com.sist.nbgb.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sist.nbgb.dto.OfflineClassPaymentListDTO;
import com.sist.nbgb.dto.OfflineClassView;
import com.sist.nbgb.dto.OnlineClassListDTO;
import com.sist.nbgb.dto.OnlineReviewCommentDTO;
import com.sist.nbgb.dto.ReviewDTO;
import com.sist.nbgb.dto.ReviewRequestDTO;
import com.sist.nbgb.dto.ReviewUpdateDTO;
import com.sist.nbgb.entity.User;
import com.sist.nbgb.enums.Role;
import com.sist.nbgb.enums.Status;
import com.sist.nbgb.service.OnlineClassService;
import com.sist.nbgb.service.ReviewService;
import com.sist.nbgb.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    
	private final UserService userService;
	private final OnlineClassService onlineClassService;
	private final ReviewService reviewService;
	
	/*수강 후기 작성 페이지*/
    @GetMapping("/user/userReviewWrite/{classId}/{classIden}/{partnerOrderId}")
    public String reviewWrite(Model model, Principal principal, @PathVariable(value="classId") Long classId, @PathVariable(value="classIden") String classIden, @PathVariable(value="partnerOrderId") String partnerOrderId){
    	User user = userService.findUserById(principal.getName());
    	if(user.getAuthority().equals(Role.ROLE_USER)) {
    		if(classIden.equals("ON")) {
    			OnlineClassListDTO onlineClass = new OnlineClassListDTO(onlineClassService.findById(classId));
    			model.addAttribute("class", onlineClass);
    			model.addAttribute("classId", onlineClass.getOnlineClassId());
    		}else {
    			OfflineClassView offlineClass = new  OfflineClassView(reviewService.offlineInfo(classId));
    			model.addAttribute("class", offlineClass);
    			model.addAttribute("classId", offlineClass.getOfflineClassId());
    		}
    		model.addAttribute("userNickname", user.getUserNickname());
    	}
    	
    	model.addAttribute("classIden", classIden);
    	model.addAttribute("partnerOrderId", partnerOrderId);
    	return "mypage/review/userReviewWrite";
    }
    
    /*수강 후기 조회 페이지*/
    @GetMapping("/user/userReviewView/{classId}/{classIden}/{partnerOrderId}")
    public String reviewView(Model model, Principal principal, @PathVariable(value="classId") Long classId, @PathVariable(value="classIden")String classIden, @PathVariable(value="partnerOrderId") String partnerOrderId) {
    	User user = userService.findUserById(principal.getName());
    	if(user.getAuthority().equals(Role.ROLE_USER)) {
    		if(classIden.equals("ON")) {
    			OnlineClassListDTO onlineClass = new OnlineClassListDTO(onlineClassService.findById(classId));
    			model.addAttribute("class", onlineClass);
    			model.addAttribute("classId", onlineClass.getOnlineClassId());
    		}else {
    			OfflineClassView offlineClass = new OfflineClassView(reviewService.offlineInfo(classId));
    			model.addAttribute("class", offlineClass);
    			model.addAttribute("classId", offlineClass.getOfflineClassId());
    		}
    		
			ReviewDTO review = reviewService.viewReview(partnerOrderId);
			model.addAttribute("review", review);
			if(reviewService.exsitsComment(review.getReviewId())) {
				OnlineReviewCommentDTO comment = reviewService.viewReviewComment(review.getReviewId());
				model.addAttribute("comment", comment);
			}
    	}
    	model.addAttribute("classIden", classIden);
    	model.addAttribute("userNickname", user.getUserNickname());
    	model.addAttribute("partnerOrderId", partnerOrderId);
    	return "mypage/review/userReviewView";
    }
    
    /*수강 후기 수정 페이지*/
    @GetMapping("/user/userReviewUpdate/{classId}/{classIden}/{partnerOrderId}")
    public String reviewUpdate(Model model, Principal principal, @PathVariable Long classId, @PathVariable(value="classIden") String classIden, @PathVariable(value="partnerOrderId") String partnerOrderId){
    	User user = userService.findUserById(principal.getName());
    	if(user.getAuthority().equals(Role.ROLE_USER)) {
    		if(classIden.equals("ON")) {
    			OnlineClassListDTO onlineClass = new OnlineClassListDTO(onlineClassService.findById(classId));
    			model.addAttribute("class", onlineClass);
    			model.addAttribute("classId", onlineClass.getOnlineClassId());
    		}else {
    			OfflineClassView offlineClass = new OfflineClassView(reviewService.offlineInfo(classId));
    			model.addAttribute("class", offlineClass);
    			model.addAttribute("classId", offlineClass.getOfflineClassId());
    		}
    		
			ReviewDTO review = reviewService.viewReview(partnerOrderId);
			model.addAttribute("review", review);
    		model.addAttribute("userNickname", user.getUserNickname());
    	}
    	
    	model.addAttribute("classIden", classIden);
    	model.addAttribute("partnerOrderId", partnerOrderId);
    	return "mypage/review/userReviewUpdate";
    }
    
    /*수강후기 작성,삭제,수정*/
    @ResponseBody
    @PostMapping("/user/userReview/{classIden}")
    public ResponseEntity<ReviewRequestDTO> reviewUpload(Principal principal, @PathVariable(value="classIden") String classIden,  @RequestParam(value="partnerOrderId") String partnerOrderId, 
    		@RequestParam(value="rating") String rating, @RequestParam(value="reviewContents") String reviewContents, @RequestParam(value="classId") Long classId){
    	User user = userService.findUserById(principal.getName());
    	ReviewRequestDTO reviewUpload = null;
    	if(user.getAuthority().equals(Role.ROLE_USER)) {
    		if(classIden.equals("ON")) {
    			reviewUpload = ReviewRequestDTO.builder()
    					.classId(classId)
    					.classIden(classIden)
    					.reviewContent(reviewContents)
    					.reviewLikeCnt(Long.valueOf(0))
    					.reviewRating(Long.valueOf(rating))
    					.reviewRegdate(LocalDateTime.now())
    					.reviewStatus(Status.Y)
    					.userId(user)
    					.partnerOrderId(partnerOrderId)
    					.build();
    			reviewService.uploadOnlineReview(reviewUpload);
    		}else {
    			reviewUpload = ReviewRequestDTO.builder()
    					.classId(classId)
    					.classIden(classIden)
    					.reviewContent(reviewContents)
    					.reviewLikeCnt(Long.valueOf(0))
    					.reviewRating(Long.valueOf(rating))
    					.reviewRegdate(LocalDateTime.now())
    					.reviewStatus(Status.Y)
    					.userId(user)
    					.partnerOrderId(partnerOrderId)
    					.build();
    			reviewService.uploadOnlineReview(reviewUpload);
    		}
    	}
    	return ResponseEntity.ok().body(reviewUpload);
    }
    
    @ResponseBody
    @DeleteMapping("/user/userReview")
    public ResponseEntity<Void> reviewUpdate(Principal principal, @RequestParam(value="reviewId") Long reviewId){
    	User user = userService.findUserById(principal.getName());
    	if(user.getAuthority().equals(Role.ROLE_USER)) {
			if(reviewService.exsitsReview(reviewId)) {
				reviewService.deleteReview(reviewId);
			}
    	}
    	
    	return ResponseEntity.ok().build();
    }
    
    @ResponseBody
    @PutMapping("/user/userReview")
    public ResponseEntity<ReviewUpdateDTO> reviewUpdate(Principal principal, @RequestParam(value="reviewId") String reviewId, 
    		@RequestParam(value="rating") String rating, @RequestParam(value="reviewContents") String reviewContents){
    	User user = userService.findUserById(principal.getName());
    	ReviewUpdateDTO updateReview = null;
    	if(user.getAuthority().equals(Role.ROLE_USER)) {
			if(reviewService.exsitsReview(Long.valueOf(reviewId))) {
				updateReview = new ReviewUpdateDTO(reviewContents, Long.valueOf(rating));
				reviewService.updateReview(Long.valueOf(reviewId), updateReview);
			}
    	}
    	
    	return ResponseEntity.ok().body(updateReview);
    }
    
    /*오프라인 리뷰 작성*/
}
