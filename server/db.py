from langchain_milvus import Milvus
from langchain_community.embeddings import OllamaEmbeddings


class DB():
    def __init__(self, embeddings, connection_args):
        self.embeddings = embeddings
        self.connection_args = connection_args

        self.vector_db: dict[str, Milvus] = {}

    def build_vector_store(self, documents, ids, collection_name="default"):
        self.vector_db[collection_name] = Milvus.from_texts(
            texts=documents,
            embedding=self.embeddings,
            connection_args=self.connection_args,
            drop_old=True,
            collection_name=collection_name,
            ids=ids
        )

    def query(self, query_vector, collection_name, k=1):
        if not self.vector_db[collection_name]:
            raise Exception("Vector store not built yet")

        return self.vector_db[collection_name].similarity_search(query_vector, k=k)


# --------------------------------------------------------------------------------


def main():
    db = DB(embeddings=OllamaEmbeddings(model="all-minilm"), connection_args={
        "uri": "./temp/milvus.db",
    })

    db.build_vector_store(
        ["hello", "world", "foo", "bar"], collection_name="geo")

    data = db.query("hello", 3)

    for item in data:
        print(item.page_content)


if __name__ == '__main__':
    main()
