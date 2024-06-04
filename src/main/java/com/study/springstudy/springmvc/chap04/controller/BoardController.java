package com.study.springstudy.springmvc.chap04.controller;

import com.study.springstudy.springmvc.chap04.common.PageMaker;
import com.study.springstudy.springmvc.chap04.common.Search;
import com.study.springstudy.springmvc.chap04.dto.BoardDetailResponseDto;
import com.study.springstudy.springmvc.chap04.dto.BoardListResponseDto;
import com.study.springstudy.springmvc.chap04.dto.BoardWriteRequestDto;
import com.study.springstudy.springmvc.chap04.service.BoardService;
import com.study.springstudy.springmvc.chap05.dto.response.ReactionDto;
import com.study.springstudy.springmvc.chap05.service.ReactionService;
import com.study.springstudy.springmvc.util.LoginUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

//    private final BoardRepository repository;
    private final BoardService boardservice;
    private final ReactionService reactionService;

    // 1. 목록 조회 요청 (/board/list: GET)
    @GetMapping("/list")
    public String list(@ModelAttribute("s") Search page, Model model) {
        System.out.println("/board/list GET!!");
        // 1. 데이터베이스로부터 게시글 목록 조회
        // 2. 클라이언트에 데이터를 보내기전에 렌더링에 필요한 데이터만 추출하기
        // 서비스에게 조회 요청 위임
        List<BoardListResponseDto> blist = boardservice.findList(page);
        // 페이지 정보를 생성하여 JSP에게 전송
        PageMaker maker = new PageMaker(page, boardservice.getCount(page));

        // 3. JSP 파일에 해당 목록 데이터를 보냄
        model.addAttribute("bList", blist);
        model.addAttribute("maker", maker);
//        model.addAttribute("s", page);
        return "board/list";
    }

    // 2. 게시글 쓰기 양식 화면 열기 요청 (/board/write: GET)
    @GetMapping("/write")
    public String write() {
        System.out.println("/board/write GET!!");
        return "board/write";

    }

    // 3. 게시글 등록 요청 (/board/write: POST)
    // => 목록조회 요청 리다이렉션
    @PostMapping("/write")
    public String write(BoardWriteRequestDto dto, HttpSession session) {
        System.out.println("/board/write POST!!");
        // 1. 브라우저가 전달한 게시글 내용 읽기
        System.out.println("dto = " + dto);
        // 2. 해당 게시글을 데이터베이스에 저장하기 위해 엔터티 클래스로 변환
        // 3. 데이터베이스 저장 명령
        boardservice.insert(dto, session);

        return "redirect:/board/list";
    }

    // 4. 게시글 삭제 요청 (/board/delete: GET)
    //     => 목록조회 요청 리다이렉션
    @GetMapping("/delete")
    public String delete(int bno) {
        System.out.println("/board/delete GET!!");
        boardservice.remove(bno);
        return "redirect:/board/list";
    }

    // 5. 게시글 상세 조회 요청 (/board/detail : GET)
    @GetMapping("/detail")
    public String detail(int bno, Model model, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("/board/detail GET!!");

        // 1. 상세조회하고싶은 글 번호를 읽기
        System.out.println("bno = " + bno);
        // 2. 데이터베이스로부터 해당 글 번호 데이터 조회하기
        BoardDetailResponseDto dto = boardservice.detail(bno, request, response);
        // 3. JSP 파일에 조회한 데이터 보내기
        model.addAttribute("bbb", dto);
        // 4. 요청 헤더를 파싱하여 이전 페이지의 주소를 얻어냄
        String ref = request.getHeader("Referer");
        model.addAttribute("ref", ref);

        return "board/detail";
    }

    // 좋아요 요청 비동기 처리
    @GetMapping("/like")
    @ResponseBody
    public ResponseEntity<?> like(long bno, HttpSession session) {

        // 로그인 검증
        if(!LoginUtil.isLoggedIn(session)) {
            return ResponseEntity.status(403)
                    .body("로그인이 필요합니다.");
        }

        log.info("like async request!");

        String account = LoginUtil.getLoggedInUserAccount(session);

        ReactionDto dto = reactionService.like(bno, account);// 좋아요 요청 처리

        return ResponseEntity.ok().body(dto);
    }

    // 싫어요 요청 비동기 처리
    @GetMapping("/dislike")
    @ResponseBody
    public ResponseEntity<?> dislike(long bno, HttpSession session) {

        // 로그인 검증
        if(!LoginUtil.isLoggedIn(session)) {
            return ResponseEntity.status(403)
                    .body("로그인이 필요합니다.");
        }

        log.info("dislike async request!");

        String account = LoginUtil.getLoggedInUserAccount(session);

        ReactionDto dto = reactionService.dislike(bno, account);// 싫어요 요청 처리

        return ResponseEntity.ok().body(dto);
    }



}
