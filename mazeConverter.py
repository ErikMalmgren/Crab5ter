# Använd detta verktyget för att generera input https://www.dcode.fr/maze-generator

input_str =  """0001111111
                0000000001
                1001111001
                1000001001
                1001111111
                1001001001
                1001001001
                1000001001
                1001001001
                100100000
                111111100"""

# Dela upp strängen i rader
input_rows = input_str.split("\n")

# Skapa en matris med rätt storlek fylld med nollor
output_matrix = [[0] * len(input_rows[0]) for _ in range(len(input_rows))]

# Fyll i matrisen baserat på input-strängen
for i in range(len(input_rows)):
    for j in range(len(input_rows[0])):
        if input_rows[i][j] == "1":
            output_matrix[i][j] = 1
        elif input_rows[i][j] == "0":
            output_matrix[i][j] = 0
        elif input_rows[i][j] == "-":
            output_matrix[i][j] = -1

# Skriv ut matrisen som kod som kan kopieras in i Java
print("{")
for row in output_matrix:
    print("    {" + ",".join(str(x) for x in row) + "},")
print("}")
