package com.tanyiqu.filesafe.adapter;


import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.tanyiqu.filesafe.R;
import com.tanyiqu.filesafe.exception.NoSuchFileToPlayException;
import com.tanyiqu.filesafe.utils.ToastUtil;
import com.tanyiqu.filesafe.utils.Util;

import java.io.File;
import java.util.List;

/**
 * 适配器类
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private List<FileView> fileViewList;

    //ViewHolder类
    public class ViewHolder extends RecyclerView.ViewHolder {

        //主视图 用于点击/长按事件
        public ConstraintLayout files_item;
        public ImageView img_files_logo;
        public TextView tv_file_name;
        public TextView tv_file_size;
        public TextView tv_file_date;

        public String original_name;    //原文件名字
        public String encrypted_name;   //加密后的名字
        public String path;             //file所在的目录路径

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            files_item = itemView.findViewById(R.id.files_item);
            img_files_logo = itemView.findViewById(R.id.img_files_logo);
            tv_file_name = itemView.findViewById(R.id.tv_file_name);
            tv_file_size = itemView.findViewById(R.id.tv_file_size);
            tv_file_date = itemView.findViewById(R.id.tv_file_date);
        }
    }

    //构造
    public FilesAdapter(List<FileView> fileViewList){
        this.fileViewList = fileViewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_files_item,parent,false);
        final ViewHolder holder = new ViewHolder(root);
        final ConstraintLayout files_item = holder.files_item;
        //点击事件
        files_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开此文件
                try {
                    Util.openVideoFile(view.getContext(),new File(holder.path + File.separator + holder.encrypted_name));
                } catch (NoSuchFileToPlayException e) {
                    ToastUtil.errorToast(view.getContext(),e.getMessage());
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileView fileView = fileViewList.get(position);

//        Bitmap bmp= BitmapFactory.decodeFile(fileView.coverPath);
//        holder.img_files_logo.setImageBitmap(bmp);

        holder.tv_file_name.setText(fileView.original_name);
        holder.tv_file_size.setText(fileView.size);
        holder.tv_file_date.setText(fileView.date);
        holder.original_name = fileView.original_name;
        holder.encrypted_name = fileView.encrypted_name;
        holder.path = fileView.path;
    }

    @Override
    public int getItemCount() {
        return fileViewList.size();
    }

    public static class FileView {
        //封面
        public Drawable drawable;
        public String coverPath;
        //名字
        public String original_name;    //原文件名字
        public String encrypted_name;   //加密后的名字
        //日期
        public String date;
        //大小
        public String size;
        //file 所在的路径
        public String path;


        //原文件名 加密后文件名 日期 大小 所在路径（用于打开文件）
        public FileView(String original_name, String encrypted_name, String date, String size, String path) {
            this.original_name = original_name;
            this.encrypted_name = encrypted_name;
            this.date = date;
            this.size = size;
            this.path = path;
        }
    }

}


