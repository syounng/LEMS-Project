package lems.cowshed.api.controller.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lems.cowshed.domain.user.Mbti;
import lems.cowshed.domain.user.query.UserEventMyPageQueryDto;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "마이 페이지 회원 정보")
public class UserMyPageResponseDto {

    @Schema(description = "이름", example = "하상록")
    private String name;

    @Schema(description = "생년월일", example = "1999-05-22")
    private LocalDate birth;

    @Schema(description = "성격유형", example = "ISTP")
    private Mbti character;

    @Schema(description = "참여 모임")
    private List<UserEventMyPageQueryDto> userEventList;

    @Schema(description = "북마크 모임")
    private List<UserBookmarkResponseDto> bookmarkList;

}
