package com.scms.app.service;

import com.scms.app.model.Program;
import com.scms.app.model.ProgramStatus;
import com.scms.app.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로그램 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProgramService {

    private final ProgramRepository programRepository;

    /**
     * 모든 프로그램 조회
     */
    public List<Program> getAllPrograms() {
        return programRepository.findAllNotDeleted();
    }

    /**
     * 메인 페이지용 최신 프로그램 조회 (상위 8개)
     */
    public List<Program> getMainPagePrograms() {
        List<Program> programs = programRepository.findAllNotDeleted();
        return programs.stream()
                .limit(8)
                .collect(Collectors.toList());
    }

    /**
     * 프로그램 ID로 조회
     */
    public Program getProgram(Integer programId) {
        return programRepository.findByIdNotDeleted(programId)
                .orElseThrow(() -> new IllegalArgumentException("프로그램을 찾을 수 없습니다: ID " + programId));
    }

    /**
     * 프로그램 조회 (조회수 증가)
     */
    @Transactional
    public Program getProgramWithHitIncrement(Integer programId) {
        Program program = getProgram(programId);
        program.incrementHits();
        programRepository.save(program);
        return program;
    }

    /**
     * 카테고리별 프로그램 조회
     */
    public List<Program> getProgramsByCategory(String category) {
        return programRepository.findByCategoryNotDeleted(category);
    }

    /**
     * 행정부서별 프로그램 조회
     */
    public List<Program> getProgramsByDepartment(String department) {
        return programRepository.findByDepartmentNotDeleted(department);
    }

    /**
     * 단과대학별 프로그램 조회
     */
    public List<Program> getProgramsByCollege(String college) {
        return programRepository.findByCollegeNotDeleted(college);
    }

    /**
     * 상태별 프로그램 조회
     */
    public List<Program> getProgramsByStatus(ProgramStatus status) {
        return programRepository.findByStatusNotDeleted(status);
    }

    /**
     * 제목으로 프로그램 검색
     */
    public List<Program> searchProgramsByTitle(String keyword) {
        return programRepository.searchByTitleNotDeleted(keyword);
    }

    /**
     * 신청 가능한 프로그램 조회
     */
    public List<Program> getAvailablePrograms() {
        return programRepository.findAvailablePrograms(LocalDateTime.now());
    }

    /**
     * 인기 프로그램 조회 (조회수 기준 상위 8개)
     */
    public List<Program> getPopularPrograms() {
        return programRepository.findPopularPrograms().stream()
                .limit(8)
                .collect(Collectors.toList());
    }

    /**
     * 복합 필터로 프로그램 조회
     */
    public List<Program> getProgramsByFilters(String department, String college, String category) {
        return programRepository.findByFilters(department, college, category);
    }

    /**
     * 프로그램 생성
     */
    @Transactional
    public Program createProgram(Program program) {
        Program savedProgram = programRepository.save(program);
        log.info("프로그램 생성 완료: {} (ID: {})", savedProgram.getTitle(), savedProgram.getProgramId());
        return savedProgram;
    }

    /**
     * 프로그램 수정
     */
    @Transactional
    public Program updateProgram(Integer programId, Program programData) {
        Program program = getProgram(programId);

        // 업데이트 가능한 필드 설정
        if (programData.getTitle() != null) {
            program.setTitle(programData.getTitle());
        }
        if (programData.getDescription() != null) {
            program.setDescription(programData.getDescription());
        }
        if (programData.getContent() != null) {
            program.setContent(programData.getContent());
        }
        if (programData.getDepartment() != null) {
            program.setDepartment(programData.getDepartment());
        }
        if (programData.getCollege() != null) {
            program.setCollege(programData.getCollege());
        }
        if (programData.getCategory() != null) {
            program.setCategory(programData.getCategory());
        }
        if (programData.getSubCategory() != null) {
            program.setSubCategory(programData.getSubCategory());
        }
        if (programData.getApplicationStartDate() != null) {
            program.setApplicationStartDate(programData.getApplicationStartDate());
        }
        if (programData.getApplicationEndDate() != null) {
            program.setApplicationEndDate(programData.getApplicationEndDate());
        }
        if (programData.getMaxParticipants() != null) {
            program.setMaxParticipants(programData.getMaxParticipants());
        }
        if (programData.getThumbnailUrl() != null) {
            program.setThumbnailUrl(programData.getThumbnailUrl());
        }
        if (programData.getStatus() != null) {
            program.setStatus(programData.getStatus());
        }

        Program updatedProgram = programRepository.save(program);
        log.info("프로그램 수정 완료: {} (ID: {})", updatedProgram.getTitle(), updatedProgram.getProgramId());
        return updatedProgram;
    }

    /**
     * 프로그램 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteProgram(Integer programId) {
        Program program = getProgram(programId);
        program.delete();
        programRepository.save(program);
        log.info("프로그램 삭제 완료: {} (ID: {})", program.getTitle(), program.getProgramId());
    }

    /**
     * 프로그램 참가자 증가
     */
    @Transactional
    public boolean incrementParticipants(Integer programId) {
        Program program = getProgram(programId);
        if (!program.isApplicationAvailable()) {
            log.warn("신청 불가능한 프로그램: {} (ID: {})", program.getTitle(), program.getProgramId());
            return false;
        }

        boolean success = program.incrementParticipants();
        if (success) {
            // 정원이 찬 경우 상태 변경
            if (program.getMaxParticipants() != null
                && program.getCurrentParticipants() >= program.getMaxParticipants()) {
                program.setStatus(ProgramStatus.FULL);
            }
            programRepository.save(program);
            log.info("프로그램 참가자 증가: {} (ID: {}, 현재: {}/{})",
                    program.getTitle(), program.getProgramId(),
                    program.getCurrentParticipants(), program.getMaxParticipants());
        }
        return success;
    }

    /**
     * 프로그램 참가자 감소
     */
    @Transactional
    public void decrementParticipants(Integer programId) {
        Program program = getProgram(programId);
        program.decrementParticipants();

        // 정원이 다시 여유가 생긴 경우 상태 변경
        if (program.getStatus() == ProgramStatus.FULL
            && (program.getMaxParticipants() == null
            || program.getCurrentParticipants() < program.getMaxParticipants())) {
            program.setStatus(ProgramStatus.OPEN);
        }

        programRepository.save(program);
        log.info("프로그램 참가자 감소: {} (ID: {}, 현재: {}/{})",
                program.getTitle(), program.getProgramId(),
                program.getCurrentParticipants(), program.getMaxParticipants());
    }
}
