package main

import (
	"fmt"
	"io/ioutil"
	"sort"
	"strings"
	"sync"
	"time"
)

func main() {
	filename := "ulyss10.txt"
	data, _ := ioutil.ReadFile(filename)
	text := string(data)
	countWords(text)
}

func countWords(text string) int {

	var occurrence map[string]int

	separators := []string{" ", "\n", ",", ".", "-", ";", "(", ")", "\t", "\r", "!", "?", "'"}

	for i := 0; i < 3; i++ {
		occurrence = performTestLoop(text, separators)
	}

	mx := sync.WaitGroup{}

	startTime := time.Now().UnixNano() / 1000000

	for i := 0; i < 10; i++ {
		go func() {
			mx.Add(1)
			occurrence = performTestLoop(text, separators)
			go performTestLoop(text, separators)
			mx.Done()
		}()
	}

	occurrence = performTestLoop(text, separators)

	mx.Wait()
	stopTime := time.Now().UnixNano() / 1000000

	sum := 0
	for _, v := range occurrence {
		sum += v
	}

	keys := make([]string, 0, len(occurrence))
	for key := range occurrence {
		keys = append(keys, key)
	}
	lessFunction := func(i1, i2 int) bool {
		return occurrence[keys[i1]] < occurrence[keys[i2]] || (occurrence[keys[i1]] == occurrence[keys[i2]] && keys[i1] < keys[i2])
	}
	sort.Slice(keys, lessFunction)

	for _, k := range keys {
		fmt.Printf("%v, %v\n", k, occurrence[k])
	}

	fmt.Printf("SUM: %v\n", sum)
	fmt.Printf("DURATION: %v\n", (stopTime - startTime))

	return sum
}

func performTestLoop(text string, separators []string) map[string]int {
	var result = make(map[string]int)
	for i := 0; i < 100; i++ {
		split(text, result, separators)
	}
	return result
}

func split(text string, occurrence map[string]int, separators []string) {
	words := strings.Split(text, separators[0])
	for _, w := range words {
		if len(w) > 0 {
			if len(separators) > 1 {
				split(w, occurrence, separators[1:])
			} else {
				occurrence[w] = occurrence[w] + 1
			}
		}
	}
}

//func accWord(word string, occurrence map[string]int) {
//	if len(word) > 0 {
//		occurrence[word] = occurrence[word] + 1
//	}
//}
