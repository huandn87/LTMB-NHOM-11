package com.example.voltapp.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.voltapp.R;
import com.example.voltapp.model.Review;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.txtUsername.setText(review.username != null ? review.username : "Ẩn danh");
        holder.txtComment.setText(review.comment);
        holder.txtTime.setText(review.createdAt != null ? review.createdAt.substring(0, 10) : "Vừa xong");
        
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < review.rating; i++) stars.append("★");
        for (int i = review.rating; i < 5; i++) stars.append("☆");
        holder.txtRating.setText(stars.toString());
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtRating, txtComment, txtTime;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUsername = itemView.findViewById(R.id.TXT_USERNAME_REVIEW);
            txtRating = itemView.findViewById(R.id.TXT_RATING_STARS);
            txtComment = itemView.findViewById(R.id.TXT_COMMENT_REVIEW);
            txtTime = itemView.findViewById(R.id.TXT_TIME_REVIEW);
        }
    }
}
