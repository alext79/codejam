from itertools import islice
case = 'Case #%d: %s\n'
bad='Bad magician!'
vol ='Volunteer cheated!'
fin = 'mag.in'
res ='res.txt'
fout = open(res,'w')
fin = open(fin)
testcase = int(list(islice(fin,1))[0])
currcase =1


def checkCase(caseNumber):
    frow = int(list(islice(fin,1))[0])
    firstMatrix = list(islice(fin,4))
    srow = int(list(islice(fin,1))[0])
    secondMatrix = list(islice(fin,4))
    frow = set(firstMatrix[frow-1].split())
    srow = set(secondMatrix[srow-1].split())
    intersct = frow & srow
    l = len(intersct)
    s=''
    if l == 0:
        s=vol
    if l>1:
        s=bad
    if l == 1:
        s = intersct.pop()
    fout.write(case % (caseNumber,s))

while currcase<=testcase:
    checkCase(currcase)
    currcase+=1
fout.close()
    
