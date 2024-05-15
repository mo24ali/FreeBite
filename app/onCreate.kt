override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.sInscBtn.setOnClickListener {
        val scInt = Intent( this@MainActivity, SignUpActivity::class.java)
        startActivity(scInt)
    }

    binding.button3.setOnClickListener {
        val conInt = Intent(this@MainActivity, LogInActivity::class.java)
        startActivity(conInt)
    }
}