public class VideoActivity extends AppCompatActivity {

    VideoView videoView;
    TextView name,date,time,duration,caption;

    RelativeLayout layout;
    ProgressBar pgbar;

    FirebaseDatabase database;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        videoView=findViewById(R.id.videoview);
        name=findViewById(R.id.uploader);
        date=findViewById(R.id.date);
        time=findViewById(R.id.time);
        duration=findViewById(R.id.duration);
        caption=findViewById(R.id.caption);
        layout=findViewById(R.id.layout);
        pgbar=findViewById(R.id.pgbar);
        MediaController mediaController = new MediaController(this);

        videoView.setMediaController(mediaController);
        videoView.start();

        database=FirebaseDatabase.getInstance();
        ref=database.getReference().child("Videos");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String url=ds.child("VideoUrl").getValue().toString();
                    String Caption=ds.child("Caption").getValue().toString();
                    String Name=ds.child("Username").getValue().toString();
                    String Date=ds.child("Date").getValue().toString();
                    String Time=ds.child("Time").getValue().toString();
                    String Duration=ds.child("VideoDuration").getValue().toString();


                    Uri videoUri = Uri.parse(url);
                    videoView.setVideoURI(videoUri);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout.setVisibility(View.VISIBLE);
                            pgbar.setVisibility(View.GONE);
                        }
                    },3000);

                    name.setText("Uploader : "+Name);
                    date.setText("Upload date : "+Date);
                    time.setText("Upload time : "+Time);
                    duration.setText("Video duration : "+Duration);
                    caption.setText(Caption);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
