author: tangbo, jiangxiao, chengyu

C ����д fatworm ���̵��л��۵Ĳ������� 



fatworm-create:
	acm, fatworm Ϊ���űȽ�С�ı����ҪΪ����ȷ�Բ���
	A, B, C, D, E �� tuple Ϊ (int, varchar(5))����С�ֱ�Ϊ 50, 6, 200, 20, 80�����ڲ��� JoinOrder ��
	
fatworm-select:
	�Ƚ϶࣬һһ�о�
	# testNaiveSelect, testProduct, testOrderBy, testUnique: �򵥵���ȷ�Բ���
	# testCalcSelect��select 1+2 ϵ�е� query���д��Ͳ��� from ��
	# testSubquery: from ��� subquery��where ��� subquery(in, exist, all...)
					�Ӵ����������Բ��� uncorrelated subquery elimination
					select id, (select name) from student
	# testGroupBy: �Կձ��  count��select ����� aggFunc��having ����� aggFunc
	# testJoinOrder: �� A,B,C,D �ĸ��� product ������Ҫ�ȳ�С�ı�����Ҫ selection pushdown
					 ��10+����� product����Ϊ���ǳ���10�����̰�Ĳ�DP��
	# testMergeJoin: һ��Ҳ�� testAlias �� Query��MergeJoin Ҫ�Ȳ� materialize ��ܶ�
;

fatworm-index:
	�򵥵� index ����

fatworm-auto:
	auto ��ȷ�Բ��ԣ��� mysql Ϊ��׼
		
fatworm-concurrency:
	���� 18 ���߳�ͬʱ������� database������ 4 ���߳� write�������� read
	Query ��֮ǰ������ Query �ظ��Ӵ���˳����ɣ�����ͬʱ����
	û���κζ�д��ͻ�����κ�˳���ִ�н����һ��

fatworm-update:
	�������� update��set id=id+1000, id=id-1000
	Ҳ�� where ��� subquery �� update
