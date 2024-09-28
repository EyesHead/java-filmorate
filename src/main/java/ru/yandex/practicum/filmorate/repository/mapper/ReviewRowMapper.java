package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.entity.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong("id"))
                .filmId(rs.getLong("film_id"))
                .userId(rs.getLong("user_id"))
                .isPositive(rs.getBoolean("is_positive"))
                .content(rs.getString("content"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
