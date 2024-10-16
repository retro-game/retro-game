import psycopg2

# Establish a connection to the PostgreSQL database
conn = psycopg2.connect(
    host="your_host",
    database="your_database",
    user="your_username",
    password="your_password"
)

# Open a cursor to perform database operations
cur = conn.cursor()

# Define the SQL query to update values in the table
query = """
    UPDATE your_table
    SET column1 = 'new_value1', column2 = 'new_value2'
    WHERE some_condition;
"""

# Execute the query
cur.execute(query)

# Commit the changes to the database
conn.commit()

# Close the cursor and database connection
cur.close()
conn.close()
