from flask import *
from db import DB
from document_splitter import get_document, split_to_chunks
from llm import LLM
from langchain_community.embeddings import OllamaEmbeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter
import os
from prompts import PROMPTS
from flask_cors import CORS, cross_origin

app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

db = DB(embeddings=OllamaEmbeddings(model="all-minilm"),
        connection_args={"uri": "./temp/milvus.db"})

llm = LLM("llama3")


@app.route('/add_document/<user_token>', methods=["post"])
@cross_origin()
def add_document(user_token):
    print(request.files.keys())
    f = request.files['file']
    f.save(f"./files/{f.filename}")

    document = get_document(f"./files/{f.filename}")

    ids, chunks = split_to_chunks(document, splitter=RecursiveCharacterTextSplitter(
        chunk_size=1500, chunk_overlap=150))

    db.build_vector_store(documents=chunks, ids=ids,
                          collection_name=user_token)

    os.remove(f"./files/{f.filename}")

    return Response("Document added successfully", status=200)


@app.route('/prompt/<user_token>', methods=["POST"])
@cross_origin()
def prompt_route(user_token):
    json_data = request.get_json()

    context_query = json_data["context_query"]
    prompt = json_data["prompt"]

    data = db.query(query_vector=context_query,
                    collection_name=user_token, k=1)

    res = ""

    for d in data:
        res += f"{d.page_content} -> Page No {d.metadata['pk']}\n\n"

    prompt = PROMPTS[json_data["type"]](prompt, res)

    response = llm.invoke(prompt)

    return Response(response, status=200)


@app.route('/')
@cross_origin()
def hello_world():
    return 'Hello World!!!'


if __name__ == '__main__':
    app.run(host="0.0.0.0")
