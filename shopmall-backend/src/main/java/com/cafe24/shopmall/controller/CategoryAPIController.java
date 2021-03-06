package com.cafe24.shopmall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cafe24.shopmall.dto.JSONResult;
import com.cafe24.shopmall.service.CategoryService;
import com.cafe24.shopmall.vo.CategoryVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 *  카테고리 Controller
 *   - 상품을 분류하기 위한 기능이다.
 *   - 하위카테고리들은 상위카테고리 번호를 가지고 있다.
 *   - 최상위 카테고리들은 상위카테고리 번호가 null 이다.
 *   
 *   - 필드 : 카테고리 번호 (pk), 상위카테고리 번호, 카테고리명
 *   
 *  1. ADMIN만 가능한 기능 
 *  	- 카테고리 등록 
 *  		- 카테고리 명은  null 허용 X
 *  		- 하위카테고리의 상위카테고리 번호는 존재해야 한다.
 *  	- 카테고리 수정
 *  		- 상위카테고리 번호와 카테고리 명만 수정 가능하다.
 *  	- 카테고리 삭제
 *			- 상위카테고리가 지워지면 하위 카테고리도 같이 지워진다. 
 *			- 상위카테고리 확인은 jQuery의 자식 노드 찾기로 진행 예정.  
 *  2. USER와 ADMIN 모두 가능한 기능
 *  	- 카테고리 조회 
 *  		- parameter가 없으면 전체리스트를 가져온다.
 *
 */

@RestController("categoryAPIController")
@RequestMapping("/api")
@Api(value="ShopMall", description="category")
public class CategoryAPIController {
	
	@Autowired
	private CategoryService categoryService;
	
	@ApiOperation(value="카테고리 등록",notes="새로운 카테고리 등록")
	@ApiImplicitParams({
		@ApiImplicitParam(name="no",value="카테고리 번호",required=false,paramType="query"),
		@ApiImplicitParam(name="catg_top_no",value="상위 카테고리 번호",required=false,paramType="query",defaultValue=""),
		@ApiImplicitParam(name="name",value="이름",required=true,paramType="query",defaultValue=""),
	})
	@PostMapping(value="/admin/category")
	public ResponseEntity<JSONResult> add(@RequestBody @Valid CategoryVo categoryVo, BindingResult errors){
		
		if(errors.hasErrors()) {
			Map<String,String> errorMessages = new HashMap<String, String>();
			for(ObjectError index : errors.getAllErrors()) {
				FieldError fe = (FieldError)index;
				errorMessages.put(fe.getField(), fe.getDefaultMessage());
			}
			return new ResponseEntity<JSONResult>(JSONResult.fail("입력 값이 올바르지 않습니다.",errorMessages),HttpStatus.BAD_REQUEST);
		}
		
		Integer no = categoryService.add(categoryVo);
		return new ResponseEntity<JSONResult>(JSONResult.success(no),HttpStatus.OK);
	}
	
	@ApiOperation(value="카테고리 조회",notes="카테고리 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name="no",value="카테고리 번호",required=false,paramType="query")
	})
	@GetMapping(value= {"/category","/category/{cate_no}"})
	public ResponseEntity<JSONResult> list(@PathVariable(value="cate_no") Optional<Integer> cate_no){
		
		Integer no = cate_no.isPresent()?cate_no.get():0;
		List<CategoryVo> results = categoryService.list(no);
		
		if(results.size()==0) {
			return new ResponseEntity<JSONResult>(JSONResult.fail("입력 값이 올바르지 않습니다.",null),HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<JSONResult>(JSONResult.success(results),HttpStatus.OK);
	}
	
	@ApiOperation(value="카테고리 정보 가져오기",notes="카테고리 정보 가져오기")
	@ApiImplicitParams({
		@ApiImplicitParam(name="no",value="카테고리 번호",required=true,paramType="query")
	})
	@GetMapping(value="/admin/category/{no}")
	public ResponseEntity<JSONResult> getCategoryInfo(@PathVariable(value="no")Integer no){
		
		CategoryVo vo = categoryService.getCategoryInfo(no);
		
		if(vo == null) {
			return new ResponseEntity<JSONResult>(JSONResult.fail("입력 값이 올바르지 않습니다.",null),HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<JSONResult>(JSONResult.success(vo),HttpStatus.OK);
	}
	
	@ApiOperation(value="카테고리 수정",notes="카테고리 수정")
	@ApiImplicitParams({
		@ApiImplicitParam(name="no",value="카테고리 번호",required=true,paramType="query"),
		@ApiImplicitParam(name="catg_top_no",value="상위 카테고리 번호",required=true,paramType="query"),
		@ApiImplicitParam(name="name",value="이름",required=true,paramType="query"),
	})
	@PutMapping(value="/admin/category")
	public ResponseEntity<JSONResult> update(@RequestBody @Valid CategoryVo categoryVo, BindingResult errors){
		
		if(errors.hasErrors()) {
			Map<String,String> errorMessages = new HashMap<String, String>();
			for(ObjectError index : errors.getAllErrors()) {
				FieldError fe = (FieldError)index;
				errorMessages.put(fe.getField(), fe.getDefaultMessage());
			}
			return new ResponseEntity<JSONResult>(JSONResult.fail("입력 값이 올바르지 않습니다.",errorMessages),HttpStatus.BAD_REQUEST);
		}
		CategoryVo result = categoryService.update(categoryVo);
		
		return new ResponseEntity<JSONResult>(JSONResult.success(result),HttpStatus.OK);
	}
	
	@ApiOperation(value="카테고리 삭제",notes="카테고리 삭제 하기")
	@ApiImplicitParams({
		@ApiImplicitParam(name="no",value="카테고리 번호",required=true,paramType="query")
	})
	@DeleteMapping(value="/admin/category/{no}")
	public ResponseEntity<JSONResult> delete(@PathVariable(value="no") Integer no){
		
		Boolean result = categoryService.delete(no);
		
		if(result == false) {
			return new ResponseEntity<JSONResult>(JSONResult.fail("입력 값이 바르지 않습니다.",null),HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<JSONResult>(JSONResult.success(result),HttpStatus.OK);
	}
}
