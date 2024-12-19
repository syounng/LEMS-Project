package lems.cowshed.service;

import lems.cowshed.api.controller.dto.user.request.UserEditRequestDto;
import lems.cowshed.api.controller.dto.user.request.UserLoginRequestDto;
import lems.cowshed.api.controller.dto.user.request.UserSaveRequestDto;
import lems.cowshed.domain.user.Mbti;
import lems.cowshed.domain.user.User;
import lems.cowshed.domain.user.UserRepository;
import lems.cowshed.domain.user.query.UserQueryRepository;
import lems.cowshed.exception.UserEditException;
import lems.cowshed.exception.UserLoginException;
import lems.cowshed.exception.UserRegisterException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserQueryRepository userQueryRepository;

    @DisplayName("신규 회원을 등록 한다.")
    @Test
    void JoinProcess() {
        //given
        String email = "test@naver.com";
        UserSaveRequestDto request = createSaveDto(email, "테스트", "tempPassword");

        //when
        userService.JoinProcess(request);

        //then
        User findUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(findUser).isNotNull()
                .extracting("username", "email")
                .containsExactly("테스트", "test@naver.com");
    }

    @DisplayName("신규 회원을 등록 할 때 이미 등록된 닉네임 혹은 이메일이 있을 경우 예외가 발생 한다.")
    @CsvSource(value = {"PriorRegister@naver.com-비등록", "NonRegister@naver.com-사전등록", "PriorRegister@naver.com-사전등록"}, delimiter = '-')
    @ParameterizedTest
    void JoinProcessWhenDuplicateNameOrEmail(String email, String username) {
        //given
        UserSaveRequestDto oldRequest = createSaveDto("PriorRegister@naver.com", "사전등록");
        userService.JoinProcess(oldRequest);

        UserSaveRequestDto request = createSaveDto(email, username);

        //when //then
        assertThatThrownBy(() -> userService.JoinProcess(request))
                .isInstanceOf(UserRegisterException.class)
                .hasMessage(request.getEmail() + " " + request.getUsername() + "은 이미 존재 하는 이름 혹은 이메일 입니다.")
                .extracting(e -> ((UserRegisterException) e).getHttpStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("회원이 로그인을 한다.")
    @Test
    void login() {
        //given
        String email = "test@naver.com";
        String validPassword = "validPassword";
        UserSaveRequestDto saveRequest = createSaveDto(email, "테스트", validPassword);

        userService.JoinProcess(saveRequest);

        User user = userRepository.findByEmail(email).orElseThrow();
        UserLoginRequestDto request = createLoginDto(email, validPassword);

        //when //then
        userService.login(request);
    }

    @DisplayName("회원이 로그인을 할때 회원 가입을 안한 이메일인 경우 예외가 발생한다.")
    @Test
    void loginWhenNotFoundUserByEmail() {
        //given
        UserLoginRequestDto request = createLoginDto("NonRegisterEmail@naver.com", "tempPassword");

        //when //then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UserLoginException.class)
                .hasMessage(request.getEmail() + " 유저를 찾을수 없습니다.")
                .extracting(e -> ((UserLoginException) e).getHttpStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName("회원이 로그인 할때 비밀번호가 틀리면 예외가 발생한다.")
    @Test
    void loginWhenNotValidationPassword() {
        //given
        String email = "test@naver.com";
        UserSaveRequestDto saveRequest = createSaveDto(email, "테스트", "validPassword");
        userService.JoinProcess(saveRequest);

        User user = userRepository.findByEmail(email).orElseThrow();
        UserLoginRequestDto request = createLoginDto(user.getEmail(), "notValidPassword");

        //when //then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UserLoginException.class)
                .hasMessage("아이디와 비밀번호를 다시 확인 해주세요")
                .extracting(e -> ((UserLoginException) e).getHttpStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("회원의 세부 정보를 수정 한다.")
    @Test
    void editProcess() {
        //given
        User user = createUser("테스트", "test@naver.com");
        userRepository.save(user);

        String editName = "수정한닉네임";
        UserEditRequestDto request = createEditDto(editName, "안녕하세요!", Mbti.INTP);

        //when
        userService.editProcess(request, user.getId());

        //then
        User findUser = userRepository.findByUsername(editName).orElseThrow();
        assertThat(findUser).extracting("username", "introduction", "mbti")
                .containsExactly(editName, "안녕하세요!", Mbti.INTP);
    }

    @DisplayName("회원의 세부 정보를 수정 할때 같은 닉네임을 가진 회원이 있다면 예외가 발생 한다.")
    @Test
    void editProcessWhenDuplicateUsername() {
        //given
        String duplicateName = "테스트";
        User priorRegisterUser = createUser(duplicateName, "test@naver.com");
        userRepository.save(priorRegisterUser);

        User user = createUser("신규", "new@naver.com");
        userRepository.save(user);

        UserEditRequestDto request = createEditDto(duplicateName, "안녕하세요!", Mbti.INTP);

        //when //then
        assertThatThrownBy(() -> userService.editProcess(request, user.getId()))
                .isInstanceOf(UserEditException.class)
                .hasMessage(request.getUsername() + " 는 이미 존재 하는 닉네임 입니다.")
                .extracting(e -> ((UserEditException) e).getHttpStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("회원의 세부 정보를 수정 할때 조회할 회원의 식별자 값이 저장소에 없다면 예외가 발생 한다")
    @Test
    void editProcessWhenNotFoundUserById() {
        //given
        User user = createUser("test", "test@naver.com");
        userRepository.save(user);

        UserEditRequestDto request = createEditDto("새닉네임", "안녕하세요!", Mbti.INTP);

        //when //then
        assertThatThrownBy(() -> userService.editProcess(request, 2L))
                .isInstanceOf(UserEditException.class)
                .hasMessage("해당하는 유저를 찾을수 없습니다.")
                .extracting(e -> ((UserEditException) e).getHttpStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    //TODO
    @DisplayName("회원이 참여 중인 소모임을 조회 한다.")
    @Test
    @Disabled
    void test() {
        //given

        //when

        //then
    }

    private User createUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .build();
    }

    private UserSaveRequestDto createSaveDto(String email, String username, String password) {
        return UserSaveRequestDto.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();
    }

    private UserSaveRequestDto createSaveDto(String email, String username) {
        return UserSaveRequestDto.builder()
                .email(email)
                .username(username)
                .password("tempPassword")
                .build();
    }

    private UserLoginRequestDto createLoginDto(String email, String password){
        return UserLoginRequestDto.builder()
                .email(email)
                .password(password)
                .build();
    }

    private UserEditRequestDto createEditDto(String username, String introduction, Mbti mbti) {
        return UserEditRequestDto.builder()
                .username(username)
                .introduction(introduction)
                .localName("대구광역시 수성구")
                .birth(LocalDate.of(2024, 11, 20))
                .character(mbti)
                .build();
    }
}