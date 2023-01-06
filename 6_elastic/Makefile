.PHONY: build
image:=fruit-populator

build:
	docker build fruit_populator -t ${image} -f fruit_populator/Dockerfile

add_single: build
	docker run --network host ${image} 1

add_100: build
	docker run --network host ${image} 100
