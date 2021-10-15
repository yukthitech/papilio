db.TEST_COL.find(
	{}
).forEach(function(doc) {
	db.TEST_COL.update(
		{"_id": doc["_id"]},
		{"$set": {"newIntVal": doc.intVal + 100}} 
	);
});