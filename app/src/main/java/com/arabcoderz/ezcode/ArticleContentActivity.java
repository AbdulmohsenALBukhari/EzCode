package com.arabcoderz.ezcode;

import static com.arabcoderz.ezcode.MainActivity.MainLink;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ArticleContentActivity extends AppCompatActivity {
    static String contentURL = MainLink + "articleContent.php";
    static String commentURL = MainLink + "showComments.php";

    private SharedPreferences shared_getData;
    private static final String KEY_PREF_NAME = "userKEY";

    TextView articleTextViewWriter, articleTextViewTitle, articleTextViewContent;
    EditText articleEditTextComment;
    RequestQueue requestQueue;
    String senderUsername, strComment;
    ListView listCommentView;
    ArrayList<ListComments> listComments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_content);
        //بداية المقالات
        findViewById(R.id.butBackArticles).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ArticleContentActivity.this, UserArticlesActivity.class));
            }
        });
        findViewById(R.id.sendCommentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComments();
            }
        });
        //انتهاء الانتقالات

        //بداية التعريفات

        shared_getData = getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);
        articleTextViewWriter = findViewById(R.id.articleWriter);
        articleTextViewTitle = findViewById(R.id.articleTitle);
        articleTextViewContent = findViewById(R.id.articleContent);
        articleEditTextComment = findViewById(R.id.addCommentField);
        listCommentView = findViewById(R.id.articleComments);
        //نهاية التعريفات
        showComment();
        getContent();

    }

    void getContent() {
//        final int[] intID = new int[1];
        JsonObjectRequest jsonObjectRequestArticles = new JsonObjectRequest(Request.Method.GET, contentURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("allarticles");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject resp = jsonArray.getJSONObject(i);
                                String id = resp.getString("article_id");
//                                intID[0] = Integer.valueOf(id);
                                if (ArticlesRecyclerViewAdapter.id == Integer.valueOf(id)) {
                                    String writer = resp.getString("article_writer");
                                    String title = resp.getString("article_title");
                                    String content = resp.getString("article_content");
                                    articleTextViewWriter.setText(writer);
                                    articleTextViewTitle.setText(title);
                                    articleTextViewContent.setText(content);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error -> Log.e("VOLLEY", "ERROR"));//جلب بيانات المقالات
        requestQueue.add(jsonObjectRequestArticles);
    }

    void sendComments() {
        strComment = articleEditTextComment.getText().toString();
        shared_getData = getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE);
        senderUsername = shared_getData.getString("enterUser", "no data");

        Response.Listener<String> responseLisener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String success = jsonObject.getString("success");

                    if (success.contains("ok")) {
                        Toast.makeText(ArticleContentActivity.this, "done", Toast.LENGTH_LONG).show(); //اظهار النص من صفحة php
                        //SharedPreferences.Editor editor = shared_save.edit();
                    } else {
                        Toast.makeText(ArticleContentActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        SendComment send_comment = new SendComment(String.valueOf(ArticlesRecyclerViewAdapter.id), senderUsername, strComment, responseLisener);
        RequestQueue queue = Volley.newRequestQueue(ArticleContentActivity.this);
        queue.add(send_comment);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    void showComment() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, commentURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("allcomments");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject resp = jsonArray.getJSONObject(i);
                                String articleId = resp.getString("article_id");
                                if(ArticlesRecyclerViewAdapter.id == Integer.valueOf(articleId)) {
                                    String writer = resp.getString("comment_writer");
                                    String date = resp.getString("comment_date");
                                    String content = resp.getString("comment_content");
                                    listComments.add(new ListComments(writer, date, content));
                                    ArticleContentActivity.this.listAllItem();
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error -> Log.e("VOLLEY", "ERROR"));
        requestQueue.add(jsonObjectRequest);
    }

    void listAllItem() {
        listAdpter lA = new listAdpter(listComments);
        listCommentView.setAdapter(lA);
    }

    class listAdpter extends BaseAdapter {
        ArrayList<ListComments> listA = new ArrayList<ListComments>();

        public listAdpter(ArrayList<ListComments> listA) {
            this.listA = listA;
        }

        @Override
        public int getCount() {
            return listA.size();
        }

        @Override
        public Object getItem(int position) {
            return listA.get(position).commentContent;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.comment_card, null);
            TextView writer = (TextView) view.findViewById(R.id.comment_writer);
            TextView date = (TextView) view.findViewById(R.id.comment_date);
            TextView content = (TextView) view.findViewById(R.id.comment_content);
            writer.setText(listA.get(position).writer);
            date.setText(listA.get(position).date);
            content.setText(listA.get(position).commentContent);

            return view;
        }
    }
}