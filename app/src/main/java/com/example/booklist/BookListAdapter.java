package com.example.booklist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookListViewHolder> {

    private List<BookInfo> bookList = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;

    public BookListAdapter(Context context, List<BookInfo> bookList) {
        layoutInflater = LayoutInflater.from(context);
        this.bookList = bookList;
        this.context = context;
    }

    class BookListViewHolder extends RecyclerView.ViewHolder {

        final TextView bookTitle;
        final RatingBar bookRating;
        final TextView bookAuthor;
        final TextView publication;
        final TextView publishedDate;
        final TextView pageCount;
        final ImageView bookCover;

        public BookListViewHolder(View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.book_name);
            bookRating = itemView.findViewById(R.id.rating_bar);
            bookAuthor = itemView.findViewById(R.id.author);
            publication = itemView.findViewById(R.id.publication);
            publishedDate = itemView.findViewById(R.id.published_date);
            pageCount = itemView.findViewById(R.id.page_count);
            bookCover = itemView.findViewById(R.id.book_cover);
        }


    }


    @NonNull
    @Override
    public BookListAdapter.BookListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = layoutInflater.inflate(R.layout.template_layout, parent, false);
        return new BookListViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookListAdapter.BookListViewHolder holder, int position) {

        //set the book name or hide the book view if the book string is empty
        if (!bookList.get(position).getBookName().equals("")) {
            holder.bookTitle.setText(bookList.get(position).getBookName());
        } else {
            holder.bookTitle.setVisibility(View.GONE);
        }

        //set the book's author or hide the author view if the author string is empty
        if (!bookList.get(position).getAuthor().equals("")) {
            holder.bookAuthor.setText(bookList.get(position).getAuthor());
        } else {
            holder.bookAuthor.setVisibility(View.GONE);
        }

        //set the publication's name or hide the publication view if the publication string is empty
        if (!bookList.get(position).getPublisher().equals("")) {
            holder.publication.setText(bookList.get(position).getPublisher());
        } else {
            holder.publication.setVisibility(View.GONE);
        }

        //set the published date or hide the published date view if the date string is empty
        if (!bookList.get(position).getPublishedDate().equals("")) {
            holder.publishedDate.setText(bookList.get(position).getPublishedDate());
        } else {
            holder.publishedDate.setVisibility(View.GONE);
        }

        //set the page count or hide the view if page count is null
        if (bookList.get(position).getPageCount() != 0) {
            holder.pageCount.setText(String.valueOf(bookList.get(position).getPageCount()) + " pages");
        } else {
            holder.pageCount.setVisibility(View.GONE);
        }

        //set the book rating
        holder.bookRating.setRating(bookList.get(position).getRating());

        //set the image of the book cover
        Glide.with(context).load(bookList.get(position).getImageURL().replaceFirst("http", "https")).into(holder.bookCover);

    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}
