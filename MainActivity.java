public class MainActivity extends AppCompatActivity {

    TextView post,user;
    EditText caption;
    VideoView videoView;
    Button choose;
    android.widget.MediaController mediaController;

    String Name,Caption;

    Uri videoUri;

    FirebaseDatabase database;
    DatabaseReference ref;
    StorageReference sref;

    UploadTask uploadTask;

    AlertDialog alert;
    TextView pgtext;
    ProgressBar pgbar;
    Button cancel;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        post=findViewById(R.id.post);
        user=findViewById(R.id.user);
        caption=findViewById(R.id.caption);
        videoView=findViewById(R.id.videoview);
        choose=findViewById(R.id.choose);
        mediaController = new MediaController(this);

        database=FirebaseDatabase.getInstance();
        ref=database.getReference().child("Videos");
        sref= FirebaseStorage.getInstance().getReference("Videos");

        Name=user.getText().toString();
        videoView.setMediaController(mediaController);
        videoView.start();

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });


    }

    private void uploadVideo() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View v=getLayoutInflater().inflate(R.layout.progressbar,null);

        pgbar=v.findViewById(R.id.pgbar);
        pgtext=v.findViewById(R.id.pgtext);
        cancel=v.findViewById(R.id.cancel);

        builder.setView(v);

        alert=builder.create();
        alert.setCanceledOnTouchOutside(false);

        Caption=caption.getText().toString();
        final String date= getDate();
        final String time=getTime();

        alert.show();
        final StorageReference reference=sref.child(date+"("+time+")"+"."+getExt(videoUri));
        uploadTask = reference.putFile(videoUri);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress =  (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                pgbar.setProgress((int) progress);
                pgtext.setText("Completed : "+(int)progress+"%");
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadTask.cancel();
                alert.dismiss();
            }
        });

        Task<Uri> urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw  task.getException();
                }
                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadurl=task.getResult();
                    String videoUrl = downloadurl.toString();
                    int d=videoView.getDuration()/1000;
                    String duration=String.valueOf(d)+" s";

                    HashMap<Object , String> hashMap=new HashMap<>();
                    hashMap.put("Username",Name);
                    hashMap.put("Date",date);
                    hashMap.put("Time",time);
                    hashMap.put("VideoUrl",videoUrl);
                    hashMap.put("Caption",Caption);
                    hashMap.put("VideoDuration",duration);

                    ref.child(Name+" videos").setValue(hashMap);

                    Intent n = new Intent(MainActivity.this,VideoActivity.class);
                    n.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(n);

                    alert.dismiss();
                    caption.setText("");
                    videoView.setVisibility(View.GONE);

                }
            }
        });
    }

    private void chooseVideo() {
        Intent i = new Intent();
        i.setType("video/");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 || requestCode==RESULT_OK || data!=null || data.getData()!=null){
            videoUri=data.getData();
            videoView.setVideoURI(videoUri);
            videoView.setVisibility(View.VISIBLE);
        }
    }

    private String getExt(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String getDate(){
        Calendar c= Calendar.getInstance();
        SimpleDateFormat df=new SimpleDateFormat("dd-MM-yyy");
        return df.format(c.getTime());
    }

    private String getTime(){
        Calendar c= Calendar.getInstance();
        SimpleDateFormat df=new SimpleDateFormat("hh:mm:ss");
        return df.format(c.getTime());
    }
}
