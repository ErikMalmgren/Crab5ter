def convertToJavaMatrix(input):
  rows = input.strip().split('\n')
  matrix = [[int(cell) for cell in row.strip()] for row in rows]
  javaArray = [f"{{{','.join(str(cell) for cell in row)}}}" for row in matrix]
  return "int[][] maze = {" + ','.join(javaArray) + "};"

input = '''0001111111
0000000001
1001111001
1000001001
1001111111
1001001001
1001001001
1000001001
1001001001
100100000
111111100'''

javaMatrix = convertToJavaMatrix(input)
print(javaMatrix)