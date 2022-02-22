package main

import (
	"log"
	"math/rand"
	"strconv"
	"time"

	"github.com/streadway/amqp"
	"github.com/avast/retry-go/v4"

)

// Here we set the way error messages are displayed in the terminal.
func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}

func main() {
	// Here we connect to RabbitMQ or send a message if there are any errors connecting.
	retry.Do(
		func() error {
			conn, err := amqp.Dial("amqp://guest:guest@rabbitmq:5672/")
			if err!=nil{
				log.Printf("Retrying...")
				return err
			}
			failOnError(err, "Failed to connect to RabbitMQ")
			defer conn.Close()

			ch, err := conn.Channel()
			failOnError(err, "Failed to open a channel")
			defer ch.Close()

			// We create a Queue to send the message to.
			q, err := ch.QueueDeclare(
				"temp-queue", // name
				false,        // durable
				false,        // delete when unused
				false,        // exclusive
				false,        // no-wait
				nil,          // arguments
			)
			failOnError(err, "Failed to declare a queue")

			for {
				temp := strconv.Itoa((rand.Intn(30)))
				err = ch.Publish(
					"",     // exchange
					q.Name, // routing key
					false,  // mandatory
					false,  // immediate
					amqp.Publishing{
						ContentType: "text/plain",
						Body:        []byte(temp),
					})
				// If there is an error publishing the message, a log will be displayed in the terminal.
				failOnError(err, "Failed to publish a message")
				log.Printf(" [x] Sending temperature: %s", temp)
				time.Sleep(10 * time.Second)
			}
			return nil
		},
	)
}
