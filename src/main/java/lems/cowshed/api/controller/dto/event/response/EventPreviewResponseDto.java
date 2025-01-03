package lems.cowshed.api.controller.dto.event.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lems.cowshed.domain.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Builder
@Getter
@Schema(description = "메인 페이지의 모임 리스트 중 한 개의 모임 정보")
public class EventPreviewResponseDto {
    @Schema(description = "모임 id", example = "1")
    Long eventId;
    @Schema(description = "등록자", example = "김철수")
    String name;
    @Schema(description = "내용(20자만 노출)", example = "같이 운동하실 분 구합니다. 같이 프레스 운동 하면서 서로 보조해주실 분 구합니다.")
    String content;
    @Schema(description = "모임 날짜", example = "2024-09-12")
    LocalDateTime eventDate;
    @Schema(description = "수용 인원", example = "100")
    int capacity;
    @Schema(description = "참여 신청 인원", example = "50")
    int applicants;
    @Schema(description = "등록일", example = "yyyy-mm-dd hh:mm:ss")
    LocalDateTime createdDate;

    @QueryProjection
    public EventPreviewResponseDto(Long eventId, String name, String content, LocalDateTime eventDate, int capacity, int applicants, LocalDateTime createdDate){
        this.eventId = eventId;
        this.name = name;
        this.content = content;
        this.eventDate = eventDate;
        this.capacity = capacity;
        this.applicants = applicants;
        this.createdDate = createdDate;
    }
    public EventPreviewResponseDto(Event event) {
        this.eventId = event.getId();
        this.name = event.getName();
        this.content = event.getContent();
        this.eventDate = event.getEventDate();
        this.capacity = event.getCapacity();
        this.applicants = event.getApplicants();
    }
}