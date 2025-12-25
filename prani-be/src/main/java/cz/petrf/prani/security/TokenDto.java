package cz.petrf.prani.security;

import java.util.List;

public record TokenDto(String accessToken, String email, List<String> roles) {
}