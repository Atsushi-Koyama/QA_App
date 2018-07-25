package com.example.test.qa_app;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class QuestionDetailListAdapter extends BaseAdapter {
    //質問のレイアウトを分別するための定義
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQuestion;

    //QuestionDetailListAdapterのインスタンス化に必要
    public QuestionDetailListAdapter(Context context, Question question) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //渡されたQuestionをメンバ変数へ代入
        mQuestion = question;
    }

    @Override
    public int getCount() {
        //Questionの数+mAnswerArrayListの要素数
        return 1 + mQuestion.getAnswers().size();
    }

    //Adapterの振分
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    //今回はAdapterの種類が二種類あるため、return2
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    //position毎のitemを返す
    @Override
    public Object getItem(int position) {
        return mQuestion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //ここでいうconvertViewとはSwiftでcellのこと。
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //postionが0の時に
        if (getItemViewType(position) == TYPE_QUESTION) {
            //convertViewの生成がされていない場合は、question_detailクラスのレイアウトを保持した上でインスタンス化を行う。
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }
            String body = mQuestion.getBody();
            String name = mQuestion.getName();

            TextView bodyTextView = convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQuestion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }
        //positionが0以外の時（つまりAnswerの時）
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQuestion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }
}
