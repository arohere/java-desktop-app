from typing import Any
import pdfplumber
import time


def get_document(file: Any) -> list[str]:
    document = []

    with pdfplumber.open(file) as pdf_file:
        all_pages = pdf_file.pages

        for page_no, page in enumerate(all_pages):
            page = page.extract_text()
            document.append({
                "id": str(page_no+1),
                "text": page
            })

    return document


def split_to_chunks(document: list[dict[str, str]], splitter: Any):
    ids = []
    chunks = []

    for item in document:
        text = item["text"]
        id = item["id"]

        chunksSplit = splitter.split_text(text)

        for chunk_no, chunk in enumerate(chunksSplit):
            ids.append(f"{id}/{chunk_no+1}")
            chunks.append(chunk)

    return ids, chunks


# ----------------------------------------------------------------------------------


# from langchain_text_splitters import RecursiveCharacterTextSplitter
# from db import DB
# from langchain_community.embeddings import OllamaEmbeddings

# def main():
#     t = time.time()
#     document = get_document("./files/dsa-book.pdf")
    
#     print(f"Time taken to extract text from PDF: {time.time() - t} seconds")
    
#     timeToSplit = time.time()

#     text_splitter = RecursiveCharacterTextSplitter(
#         chunk_size=1500, chunk_overlap=150)
    
#     print(f"Time taken to split text: {time.time() - timeToSplit} seconds")
    
#     timeToSplit = time.time()

#     ids, chunks = split_to_chunks(document, splitter=text_splitter)
    
#     print(f"Time taken to split text: {time.time() - timeToSplit} seconds")
#     timeToSplit = time.time()
    
#     db = DB(embeddings=OllamaEmbeddings(model="all-minilm"), connection_args={
#         "uri": "./temp/milvus.db",
#     })
    
#     db.build_vector_store(
#         chunks, ids, collection_name="geo"
#     )
    
#     print(f"Time taken to build vector store: {time.time() - timeToSplit} seconds")
    
#     print(f"Total time taken: {time.time() - t} seconds")

# if __name__ == '__main__':
#     main()
