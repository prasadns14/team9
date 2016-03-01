from bottle import route, run, debug, template, request, static_file, error, response
import webbrowser

@route('/:filename#.*#')
def send_static(filename):
    return static_file(filename, root='./resources/public/')

@error(403)
def mistake403(code):
	return 'There is a mistake in your url!'

@error(404)
def mistake404(code):
	return 'Sorry, this page does not exist!'

def main_fn():
	webbrowser.open("http://localhost:8080/piecharts.html", new=0, autoraise=True)
	webbrowser.open("http://localhost:8080/bfa.html", new=0, autoraise=True)
	webbrowser.open("http://localhost:8080/bfc.html", new=0, autoraise=True)
	webbrowser.open("http://localhost:8080/corrmatrix.html", new=0, autoraise=True)
	webbrowser.open("http://localhost:8080/fht.html", new=0, autoraise=True)
	run(host="localhost", port=8080)
	
if __name__ == "__main__":
	main_fn()
	
