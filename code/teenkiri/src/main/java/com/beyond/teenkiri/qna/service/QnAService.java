// QnAService.java
package com.beyond.teenkiri.qna.service;

import com.beyond.teenkiri.comment.repository.CommentRepository;
import com.beyond.teenkiri.common.domain.DelYN;
import com.beyond.teenkiri.common.service.UploadAwsFileService;
import com.beyond.teenkiri.qna.domain.QnA;
import com.beyond.teenkiri.qna.dto.*;
import com.beyond.teenkiri.qna.repository.QnARepository;
import com.beyond.teenkiri.user.domain.Role;
import com.beyond.teenkiri.user.domain.User;
import com.beyond.teenkiri.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;

@Service
@Transactional(readOnly = true)
public class QnAService {

    private final QnARepository qnARepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final UploadAwsFileService uploadAwsFileService;

    @Autowired
    public QnAService(QnARepository qnARepository, UserService userService, CommentRepository commentRepository, UploadAwsFileService uploadAwsFileService) {
        this.qnARepository = qnARepository;
        this.userService = userService;
        this.commentRepository = commentRepository;
        this.uploadAwsFileService = uploadAwsFileService;
    }

    @Transactional
    public QnA createQuestion(QnASaveReqDto dto, MultipartFile imageSsr) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(userEmail);

        MultipartFile image = (imageSsr == null) ? dto.getQImage() : imageSsr;
        QnA qnA = dto.toEntity(user);

        qnA = qnARepository.save(qnA);
        try {
            if (image != null && !image.isEmpty()) {
                String bgImagePathFileName = qnA.getId() + "_question_" + image.getOriginalFilename();
                byte[] bgImagePathByte = image.getBytes();
                String s3ImagePath = uploadAwsFileService.UploadAwsFileAndReturnPath(bgImagePathFileName, bgImagePathByte);
                qnA.qUpdateImagePath(s3ImagePath);  // 질문 이미지 경로 업데이트
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
        return qnARepository.save(qnA);
    }

    public Page<QnAListResDto> qnaList(Pageable pageable) {
        Page<QnA> qnAS = qnARepository.findByDelYN(DelYN.N, pageable);
        return qnAS.map(QnA::listFromEntity);
    }

    public QnA getQuestionDetail(Long id) {
        return qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
    }

    @Transactional
    public QnA answerQuestion(Long id, QnAAnswerReqDto dto, MultipartFile imageSsr) {
        User answeredBy = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (answeredBy == null || answeredBy.getRole() != Role.ADMIN) {
            throw new SecurityException("권한이 없습니다.");
        }
        QnA qnA = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 답변 내용 업데이트
        qnA.answerQuestion(dto.getAnswerText(), answeredBy);

        MultipartFile image = (imageSsr == null) ? dto.getAImage() : imageSsr;
        try {
            if (image != null && !image.isEmpty()) {
                String bgImagePathFileName =  qnA.getId() + "_answer_" + image.getOriginalFilename();
                byte[] bgImagePathByte = image.getBytes();
                String s3ImagePath = uploadAwsFileService.UploadAwsFileAndReturnPath(bgImagePathFileName, bgImagePathByte);
                qnA.aUpdateImagePath(s3ImagePath);  // 답변 이미지 경로 업데이트
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
        return qnARepository.save(qnA);
    }

    // 질문 수정
    @Transactional
    public void QnAQUpdate(Long id, QnAQtoUpdateDto dto, MultipartFile imageSsr) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        QnA qnA = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
        MultipartFile image = (imageSsr != null) ? imageSsr : dto.getQImage();

        if (qnA.getUser().getEmail().equals(userEmail)) {
            try {
                MultipartFile imageFile = image;
                if (!imageFile.isEmpty()) {
                    String bgImagePathFileName = qnA.getId() + "_question_" + imageFile.getOriginalFilename();
                    byte[] bgImagePathByte = imageFile.getBytes();
                    String s3ImagePath = uploadAwsFileService.UploadAwsFileAndReturnPath(bgImagePathFileName, bgImagePathByte);
                    qnA.QnAQUpdate(dto, s3ImagePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("게시글 수정에 실패했습니다.");
            }
        } else {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }
        qnARepository.save(qnA);
    }

    @Transactional
    public void QnAAUpdate(Long id, QnAAtoUpdateDto dto, MultipartFile imageSsr) {
        QnA qnA = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User answeredBy = userService.findByEmail(userEmail);
        if (answeredBy == null || answeredBy.getRole() != Role.ADMIN) {
            throw new SecurityException("권한이 없습니다.");
        }
        MultipartFile image = (imageSsr != null) ? imageSsr : dto.getAImage();

        if (qnA.getAnswerer().getEmail().equals(userEmail)) {
            try {
                MultipartFile imageFile = image;
                if (!imageFile.isEmpty()) {
                    String bgImagePathFileName = qnA.getId() + "_answer_" + imageFile.getOriginalFilename();
                    byte[] bgImagePathByte = imageFile.getBytes();
                    String s3ImagePath = uploadAwsFileService.UploadAwsFileAndReturnPath(bgImagePathFileName, bgImagePathByte);
                    qnA.QnAAUpdate(dto, s3ImagePath);
//                post.updateImagePath(s3ImagePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("게시글 수정에 실패했습니다.");
            }
        } else {
            throw new IllegalArgumentException("작성자 본인만 수정할 수 있습니다.");
        }
        qnARepository.save(qnA);
    }

    @Transactional
    public QnA qnaDelete(Long id) {
        QnA qnA = qnARepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));
        qnA.updateDelYN(DelYN.Y);
        return qnA;
    }
}
