//npm install body-parser

const http = require('http')
const port = 3000
const bodyParser = require('body-parser')

const server = http.createServer(function(req, res){
    if(req.method === 'POST'){
        bodyParser.json()(req, res, function(err) {
            if (err) {
                console.log(err);
                res.statusCode = 400;
                res.end(`Error: ${err.message}`);
                return;
            }
			const jsonData = req.body;
			const email = jsonData.email;
			const message = jsonData.message;
			console.log(`${email}: ${message}`);
			res.statusCode = 200;
			res.setHeader('Content-Type', 'application/json');
			res.end(JSON.stringify(jsonData));
        });
    }else{
        res.end('Hello Node');
    }
});


server.listen(port , function(error){
	if(error){
		console.log('Something went wrong. ', error)
	}else{
		console.log('Server is listening to port ' + port)
	}
})
