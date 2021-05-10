import os
from flask import Flask, request, render_template, jsonify
from werkzeug.utils import secure_filename
from flask import send_from_directory
import datetime

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False

# upload path
UPLOAD_PATH = os.path.join(os.path.dirname(__file__), 'videos')


@app.route('/upload', methods=['GET', 'POST'])
def settings():
    if request.method == 'GET':
        # reponse
        return render_template("upload_page.html")
    else:

        # update the file
        file = request.files.get('file')
        if all([file]):
            # save the file
            filename = secure_filename(file.filename)
            file.save(os.path.join(UPLOAD_PATH, filename))
            msg = "upload success,the file address is：http://127.0.0.1:5000/videos/{}".format(filename)
            data = {
                "code": 0,
                "data": {
                    "filename": filename,
                    "datetime": datetime.datetime.now()
                },
                "msg": msg
            }
        else:
            # reponse fail：
            data = {
                "code": 1,
                "data": None,
                "msg": "parameter can not be null！"
            }

        return jsonify(data)


@app.route('/show')
def ab():
    return render_template('upload.html')
