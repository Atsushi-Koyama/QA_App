package com.example.test.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.support.design.widget.Snackbar;

import java.util.HashMap;
import java.lang.String;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity implements View.OnClickListener,DatabaseReference.CompletionListener {

    private int favoriteFL;
    private Button mFavoriteButton;

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;
    private DatabaseReference mFavoriteRef;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            //二回目のnotifyDataSetChangedでAnswerクラスをlistViewへ反映させる
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            //ログインしていない場合は、お気に入りボタンを消す
            mFavoriteButton.setVisibility(View.INVISIBLE);

        }else{
            //ログイン済みの場合は、お気に入りボタンを表示
            mFavoriteButton.setVisibility(View.VISIBLE);
        }

        if (user != null) {

            //firebaseの準備
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            //favorite/userId/QuestionIdのディレクトリにアクセス
            mFavoriteRef = databaseReference.child(Const.FavoritePATH).child(String.valueOf(user.getUid())).child(String.valueOf(mQuestion.getQuestionUid()));
            //すでに該当ディレクトリにインスタンスがある場合
            mFavoriteRef.addChildEventListener(mFavoriteListener);
        }
    }

    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            favoriteFL = 1;
            mFavoriteButton.setText("お気に入りから削除");
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public QuestionDetailActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);
        //firebaseから現在ヨグイン中のユーザーを取得する。
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mFavoriteButton = findViewById(R.id.favorite_button);
        mFavoriteButton.setOnClickListener(this);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        if (user == null) {
            //ログインしていない場合は、お気に入りボタンを消す
            mFavoriteButton.setVisibility(View.INVISIBLE);

        }else{
            //ログイン済みの場合は、お気に入りボタンを表示
            mFavoriteButton.setVisibility(View.VISIBLE);
        }

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        //Adapterのインスタンス化Viewとメンバ変数を渡す。
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        //mListViewへセット
        mListView.setAdapter(mAdapter);
        //Adapterへ再表示をさせる。
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
        //ログインしていれば
        if (user != null) {
            //firebaseの準備
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            //favorite/userId/QuestionIdのディレクトリにアクセス
            mFavoriteRef = databaseReference.child(Const.FavoritePATH).child(String.valueOf(user.getUid())).child(String.valueOf(mQuestion.getQuestionUid()));
            //すでに該当ディレクトリにインスタンスがある場合
            mFavoriteRef.addChildEventListener(mFavoriteListener);
        }
    }

//    @Override
    public void onClick(View v) {
        //現在のログインユーザーの取得
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //firebaseの取得の準備
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        //favoritePathのユーザーの生成、その下にお気に入りを下ユーザーIDの生成その下にQuestionIDのユーザのIDの生成
        mFavoriteRef = databaseReference.child(Const.FavoritePATH).child(String.valueOf(user.getUid())).child(String.valueOf(mQuestion.getQuestionUid()));

        if (favoriteFL == 0) {
            Map<String, String> data = new HashMap<String, String>();
            //QuestionIDのユーザのIDの下にQuestionのIDを付与。キーはジャンル
            data.put("ジャンル", String.valueOf(mQuestion.getGenre()));
            //completionListenerを使用することで、onCompleteが呼ばれる。
            mFavoriteRef.setValue(data, this);
            mFavoriteButton.setText("お気に入りから削除");
            favoriteFL = 1;
        }else {
            mFavoriteRef.removeValue();
            mFavoriteButton.setText("お気に入り登録");
            favoriteFL = 0;
        }
    }
    //completionListenerで呼ばれる
    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError == null) {
            Snackbar.make(findViewById(android.R.id.content), "お気に入り追加しました", Snackbar.LENGTH_LONG).show();
        }else{
            Snackbar.make(findViewById(android.R.id.content), "お気に入り追加に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }
}
