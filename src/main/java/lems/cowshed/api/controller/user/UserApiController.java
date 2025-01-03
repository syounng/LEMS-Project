package lems.cowshed.api.controller.user;

import jakarta.validation.Valid;
import lems.cowshed.api.controller.SecurityContextUtil;
import lems.cowshed.api.controller.CommonResponse;
import lems.cowshed.api.controller.dto.user.request.UserEditRequestDto;
import lems.cowshed.api.controller.dto.user.request.UserLoginRequestDto;
import lems.cowshed.api.controller.dto.user.request.UserSaveRequestDto;
import lems.cowshed.api.controller.dto.user.response.UserEventResponseDto;
import lems.cowshed.api.controller.dto.user.response.UserMyPageResponseDto;
import lems.cowshed.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserApiController implements UserSpecification{

    private final UserService userService;
    private final Long userId = SecurityContextUtil.getUserId();

    @PostMapping("/login")
    public CommonResponse<Void> login(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto){
        userService.login(userLoginRequestDto);
        return CommonResponse.success();
    }

    //TODO
    @GetMapping
    public CommonResponse<UserMyPageResponseDto> userMyPage(){
        return null;
    }

    @PostMapping("/register")
    public CommonResponse<Void> saveUser(@Valid @RequestBody UserSaveRequestDto userSaveRequestDto) {
        userService.JoinProcess(userSaveRequestDto);
        return CommonResponse.success();
    }

    @PatchMapping
    public CommonResponse<Void> editUser(@RequestBody UserEditRequestDto userEditRequestDto){
        userService.editProcess(userEditRequestDto, userId);
        return CommonResponse.success();
    }

    @GetMapping("/events")
    public CommonResponse<UserEventResponseDto> findUserEvent(){
        LocalDate now = LocalDate.now();
        UserEventResponseDto userEvent = userService.getUserEvent(now);
        return CommonResponse.success(userEvent, "유저 이벤트 조회 성공");
    }
}

