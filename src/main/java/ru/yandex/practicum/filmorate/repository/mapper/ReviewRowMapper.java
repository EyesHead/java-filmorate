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
        Review review = new Review(
                rs.getLong("id"),
                rs.getLong("film_id"),
                rs.getLong("user_id"),
                rs.getBoolean("is_positive"),
                rs.getString("content"),
                rs.getInt("useful")
        );
        return review;
    }
}
