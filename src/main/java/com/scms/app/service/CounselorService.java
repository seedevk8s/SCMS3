package com.scms.app.service;

import com.scms.app.dto.CounselorResponse;
import com.scms.app.dto.CounselorScheduleRequest;
import com.scms.app.model.Counselor;
import com.scms.app.model.CounselorSchedule;
import com.scms.app.repository.CounselorRepository;
import com.scms.app.repository.CounselorScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상담사 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CounselorService {

    private final CounselorRepository counselorRepository;
    private final CounselorScheduleRepository scheduleRepository;

    /**
     * 모든 상담사 조회
     */
    public List<CounselorResponse> getAllCounselors() {
        return counselorRepository.findAllNotDeleted().stream()
                .map(CounselorResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상담사 ID로 조회
     */
    public Counselor getCounselorById(Integer counselorId) {
        return counselorRepository.findByIdAndNotDeleted(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: ID " + counselorId));
    }

    /**
     * 전문분야로 상담사 검색
     */
    public List<CounselorResponse> searchBySpecialty(String specialty) {
        return counselorRepository.findBySpecialtyContainingAndNotDeleted(specialty).stream()
                .map(CounselorResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상담사 일정 등록
     */
    @Transactional
    public CounselorSchedule addSchedule(Integer counselorId, CounselorScheduleRequest request) {
        request.validate();

        Counselor counselor = getCounselorById(counselorId);

        CounselorSchedule schedule = CounselorSchedule.builder()
                .counselor(counselor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isAvailable(request.getIsAvailable())
                .build();

        CounselorSchedule saved = scheduleRepository.save(schedule);

        log.info("상담사 일정 등록: 상담사 ID {}, 요일 {}, 시간 {}~{}",
                counselorId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        return saved;
    }

    /**
     * 상담사의 일정 조회
     */
    public List<CounselorSchedule> getSchedulesByCounselorId(Integer counselorId) {
        return scheduleRepository.findByCounselorId(counselorId);
    }

    /**
     * 상담사의 가용 일정만 조회
     */
    public List<CounselorSchedule> getAvailableSchedules(Integer counselorId) {
        return scheduleRepository.findAvailableSchedulesByCounselorId(counselorId);
    }

    /**
     * 상담사의 특정 요일 가용 일정 조회
     */
    public List<CounselorSchedule> getAvailableSchedulesByDay(Integer counselorId, Integer dayOfWeek) {
        return scheduleRepository.findAvailableSchedulesByCounselorIdAndDayOfWeek(counselorId, dayOfWeek);
    }

    /**
     * 일정 삭제
     */
    @Transactional
    public void deleteSchedule(Integer counselorId, Long scheduleId) {
        CounselorSchedule schedule = scheduleRepository.findByIdAndNotDeleted(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: ID " + scheduleId));

        // 본인의 일정인지 확인
        if (!schedule.getCounselor().getCounselorId().equals(counselorId)) {
            throw new IllegalStateException("본인의 일정만 삭제할 수 있습니다.");
        }

        schedule.delete();
        scheduleRepository.save(schedule);

        log.info("상담사 일정 삭제: 일정 ID {}", scheduleId);
    }
}
