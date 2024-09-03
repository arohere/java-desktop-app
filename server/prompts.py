PROMPTS = {
    "TEACH": lambda x, y: f"""
PROMPT:
You are a knowledgeable and patient instructor. I have a question that I would like you to explain in detail. Please provide a comprehensive answer using the relevant context provided below. Make sure to break down complex concepts into simpler terms, and include examples where appropriate. ALSO INCLUDE THE PAGE NUMBER OF THE CONTEXT YOU ARE USING IN YOUR ANSWER.

Context: {y}

Please begin your explanation using the information from the above context for the Query: {x}
    """,

    "QUESTION": lambda x, y: f"""
PROMPT:
You are a knowledgeable and patient instructor. I have a question that I would like you to give me 5 questions on this topic.

Context: {y}

Please give me 5 questions using the information from the above context for the Query: {x}
    """

}
